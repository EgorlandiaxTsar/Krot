package com.egorgoncharov.krot.backend.service.impl;

import com.egorgoncharov.krot.backend.config.yaml.SessionConfig;
import com.egorgoncharov.krot.backend.dto.service.Result;
import com.egorgoncharov.krot.backend.model.redis.entity.SessionEntity;
import com.egorgoncharov.krot.backend.model.redis.repository.SessionRepository;
import com.egorgoncharov.krot.backend.model.relational.entity.DeviceEntity;
import com.egorgoncharov.krot.backend.model.relational.entity.HistoricalSessionEntity;
import com.egorgoncharov.krot.backend.model.relational.entity.UserEntity;
import com.egorgoncharov.krot.backend.model.relational.repository.DeviceRepository;
import com.egorgoncharov.krot.backend.model.relational.repository.HistoricalSessionRepository;
import com.egorgoncharov.krot.backend.model.relational.repository.UserRepository;
import com.egorgoncharov.krot.backend.security.Authority;
import com.egorgoncharov.krot.backend.service.AuthenticationService;
import com.egorgoncharov.krot.backend.service.helper.SecurityHelper;
import com.egorgoncharov.krot.backend.service.helper.TypesHelper;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.util.UUID;

@ApplicationScoped
public class AuthenticationServiceImpl implements AuthenticationService {
    private final SecurityIdentity client;
    private final SessionConfig sessionConfig;
    private final HistoricalSessionServiceImpl historicalSessionService;
    private final SessionRepository sessionRepository;
    private final HistoricalSessionRepository historicalSessionRepository;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;

    @Inject
    public AuthenticationServiceImpl(SecurityIdentity client, SessionConfig sessionConfig, HistoricalSessionServiceImpl historicalSessionService, SessionRepository sessionRepository, HistoricalSessionRepository historicalSessionRepository, UserRepository userRepository, DeviceRepository deviceRepository) {
        this.client = client;
        this.sessionConfig = sessionConfig;
        this.historicalSessionService = historicalSessionService;
        this.sessionRepository = sessionRepository;
        this.historicalSessionRepository = historicalSessionRepository;
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
    }

    @Override
    public Uni<Result<SessionEntity>> login(SessionEntity metadata, String identifier, String password) {
        return historicalSessionService.create(HistoricalSessionEntity.builder().userOwner(metadata.isOwnerDevice() ? null : UserEntity.builder().username(identifier).password(password).build()).deviceOwner(metadata.isOwnerDevice() ? DeviceEntity.builder().id(TypesHelper.toUUID(identifier)).password(password).build() : null).validUntil(OffsetDateTime.now().plusMinutes(sessionConfig.sessionDuration())).build()).chain(result -> {
            if (result.getCode() != 200 || result.getResult().isEmpty()) return Uni.createFrom().item(result.nullCast());
            HistoricalSessionEntity historicalSession = result.getResult().get();
            metadata.setId(UUID.randomUUID());
            metadata.setSessionReference(UUID.randomUUID());
            metadata.setEncryptionKey(SecurityHelper.generateXCC20Key());
            metadata.setValidUntil(historicalSession.getValidUntil());
            metadata.setLastUsed(historicalSession.getCreatedAt());
            metadata.setCreatedAt(historicalSession.getCreatedAt());
            return sessionRepository.save(metadata).replaceWith(Result::ok).onFailure().recoverWithNull().chain(() -> historicalSessionRepository.removeById(historicalSession.getId()).replaceWith(Result::error));
        });
    }

    @Override
    public Uni<Result<SessionEntity>> logout(UUID sessionId) {
        UserEntity clientUser = SecurityHelper.securityIdentityUser(client);
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
    }
}
