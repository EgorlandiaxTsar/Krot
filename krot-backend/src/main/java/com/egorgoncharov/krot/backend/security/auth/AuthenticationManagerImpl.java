package com.egorgoncharov.krot.backend.security.auth;

import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.application.core.session.SessionDomain;
import com.egorgoncharov.krot.backend.config.yaml.SessionConfig;
import com.egorgoncharov.krot.backend.database.redis.entity.SessionEntity;
import com.egorgoncharov.krot.backend.database.redis.repository.SessionRepository;
import com.egorgoncharov.krot.backend.database.relational.entity.DeviceEntity;
import com.egorgoncharov.krot.backend.database.relational.entity.HistoricalSessionEntity;
import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
import com.egorgoncharov.krot.backend.database.relational.repository.DeviceRepository;
import com.egorgoncharov.krot.backend.database.relational.repository.UserRepository;
import com.egorgoncharov.krot.backend.security.Authority;
import com.egorgoncharov.krot.backend.security.crypto.Generator;
import com.egorgoncharov.krot.backend.security.session.principal.PrincipalExtractor;
import com.egorgoncharov.krot.backend.util.Types;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@ApplicationScoped
public class AuthenticationManagerImpl implements AuthenticationManager {
    private final CurrentIdentityAssociation currentIdentityAssociation;
    private final SessionConfig sessionConfig;
    private final SessionDomain sessionDomain;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;

    @Inject
    public AuthenticationManagerImpl(CurrentIdentityAssociation currentIdentityAssociation, SessionConfig sessionConfig, SessionDomain sessionDomain, SessionRepository sessionRepository, UserRepository userRepository, DeviceRepository deviceRepository) {
        this.currentIdentityAssociation = currentIdentityAssociation;
        this.sessionConfig = sessionConfig;
        this.sessionDomain = sessionDomain;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
    }

    @WithTransaction
    @Override
    public Uni<Result<SessionEntity>> login(SessionEntity metadata, String identifier, String password) {
        return sessionDomain.create(HistoricalSessionEntity.builder().userOwner(metadata.isOwnerDevice() ? null : UserEntity.builder().username(identifier).password(password).build()).deviceOwner(metadata.isOwnerDevice() ? DeviceEntity.builder().id(Types.toUUID(identifier)).password(password).build() : null).validUntil(OffsetDateTime.now().plusMinutes(sessionConfig.sessionDuration())).build()).chain(result -> {
            if (result.getCode() != 200 || result.getResult().isEmpty()) return Uni.createFrom().item(result.nullCast());
            HistoricalSessionEntity historicalSession = result.getResult().get();
            metadata.setId(historicalSession.getId());
            metadata.setOwnerId(metadata.isOwnerDevice() ? historicalSession.getDeviceOwner().getId() : historicalSession.getUserOwner().getId());
            metadata.setSessionReference(UUID.randomUUID());
            metadata.setEncryptionKey(Generator.generateXCC20Key());
            metadata.setValidUntil(historicalSession.getValidUntil());
            metadata.setLastUsed(historicalSession.getCreatedAt());
            metadata.setCreatedAt(historicalSession.getCreatedAt());
            return sessionRepository.save(metadata, Duration.between(metadata.getCreatedAt(), metadata.getValidUntil())).map(Result::ok);
        });
    }

    @WithTransaction
    @Override
    public Uni<Result<SessionEntity>> logout(UUID sessionId) {
        return currentIdentityAssociation.getDeferredIdentity().chain(client -> {
            UserEntity clientUser = PrincipalExtractor.principal(client);
            if (clientUser == null) return Uni.createFrom().item(Result.forbidden());
            boolean canSelfSessionDelete = clientUser.getRole().getAuthorities().contains(Authority.SELF_SESSION_DELETE);
            boolean canDeleteAnySession = clientUser.getRole().getAuthorities().contains(Authority.X_SESSION_DELETE);
            boolean canDeleteSession = clientUser.getRole().getAuthorities().contains(Authority.SESSION_DELETE);
            if (!canSelfSessionDelete && !canDeleteAnySession && !canDeleteSession) return Uni.createFrom().item(Result.forbidden());
            return sessionRepository.findById(sessionId).chain(session -> {
                if (session == null) return Uni.createFrom().item(Result.notFound());
                if (canDeleteAnySession) return sessionRepository.removeById(sessionId).replaceWith(Result::ok);
                if (session.getOwnerId().equals(clientUser.getId()) && canSelfSessionDelete) return sessionRepository.removeById(sessionId).replaceWith(Result::ok);
                Uni<DeviceEntity> deviceOwnerUni = session.isOwnerDevice() ? deviceRepository.findById(session.getOwnerId()) : Uni.createFrom().nullItem();
                return deviceOwnerUni.map(device -> device == null ? session.getOwnerId() : device.getId()).chain(userRepository::findById).chain(owner -> {
                    if (!(owner.getRole().getGrade() < clientUser.getRole().getGrade() && canDeleteSession)) return Uni.createFrom().item(Result.forbidden());
                    return sessionRepository.removeById(sessionId).replaceWith(Result::ok);
                });
            });
        });
    }
}
