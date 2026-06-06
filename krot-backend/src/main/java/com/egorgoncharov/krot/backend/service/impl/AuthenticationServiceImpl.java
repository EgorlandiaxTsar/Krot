package com.egorgoncharov.krot.backend.service.impl;

import com.egorgoncharov.krot.backend.config.yaml.SessionConfig;
import com.egorgoncharov.krot.backend.dto.service.Result;
import com.egorgoncharov.krot.backend.dto.service.filter.RangeFilter;
import com.egorgoncharov.krot.backend.dto.service.filter.TimeRangeFilter;
import com.egorgoncharov.krot.backend.dto.service.pagination.Page;
import com.egorgoncharov.krot.backend.dto.service.pagination.PaginationOptions;
import com.egorgoncharov.krot.backend.model.common.ReactiveRepository;
import com.egorgoncharov.krot.backend.model.entity.DeviceEntity;
import com.egorgoncharov.krot.backend.model.entity.SessionEntity;
import com.egorgoncharov.krot.backend.model.entity.UserEntity;
import com.egorgoncharov.krot.backend.model.repository.DeviceRepository;
import com.egorgoncharov.krot.backend.model.repository.SessionRepository;
import com.egorgoncharov.krot.backend.model.repository.UserRepository;
import com.egorgoncharov.krot.backend.security.Authority;
import com.egorgoncharov.krot.backend.service.AuthenticationService;
import com.egorgoncharov.krot.backend.service.common.ReactiveCrudService;
import com.egorgoncharov.krot.backend.service.helper.SecurityHelper;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Parameters;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AuthenticationServiceImpl extends ReactiveCrudService<SessionEntity, UUID> implements AuthenticationService {
    private final SecurityIdentity client;
    private final SessionConfig sessionConfig;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;

    @Inject
    public AuthenticationServiceImpl(SecurityIdentity client, SessionConfig sessionConfig, SessionRepository sessionRepository, UserRepository userRepository, DeviceRepository deviceRepository) {
        this.client = client;
        this.sessionConfig = sessionConfig;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
    }

    @Override
    protected ReactiveRepository<SessionEntity, UUID> repository() {
        return sessionRepository;
    }

    @Override
    public Uni<Result<Page<SessionEntity>>> filter(List<UUID> ids, UUID userOwnerId, UUID deviceOwnerId, TimeRangeFilter lastUsedTime, TimeRangeFilter validUntilTime, TimeRangeFilter creationTime, String srcKeyQuery, String handshakeKeyQuery, String encryptionKeyQuery, PaginationOptions pagination) {
        UserEntity clientUser = SecurityHelper.securityIdentityUser(client);
        if (clientUser == null) return Uni.createFrom().item(Result.forbidden());
        StringBuilder query = new StringBuilder("1=1");
        Parameters parameters = new Parameters();
        if (!clientUser.getRole().getAuthorities().contains(Authority.X_SESSION_READ)) {
            if (clientUser.getRole().getAuthorities().contains(Authority.SESSION_READ)) {
                query.append(" AND session.userOwner.role.grade <= :clientGrade");
                parameters.and("clientGrade", clientUser.getRole().getGrade());
            } else if (clientUser.getRole().getAuthorities().contains(Authority.SELF_READ)) {
                query.append(" AND session.userOwner.id = :clientId");
                parameters.and("clientId", clientUser.getId());
                return executeFilter(query.toString(), parameters, pagination);
            } else {
                return Uni.createFrom().item(Result.forbidden());
            }
        }
        if (ids != null && !ids.isEmpty()) {
            query.append(" AND id IN :ids");
            parameters.and("ids", ids);
        }
        if (userOwnerId != null) {
            query.append(" AND userOwner.id = :userOwnerId");
            parameters.and("userOwnerId", userOwnerId);
        }
        if (deviceOwnerId != null) {
            query.append(" AND deviceOwner.id = :deviceOwnerId");
            parameters.and("deviceOwnerId", deviceOwnerId);
        }
        RangeFilter.applyRangeFilter(query, "lastUsed", parameters, lastUsedTime);
        RangeFilter.applyRangeFilter(query, "validUntil", parameters, validUntilTime);
        RangeFilter.applyRangeFilter(query, "createdAt", parameters, creationTime);
        if (srcKeyQuery != null) {
            query.append(" AND (lower(srcKey)) LIKE :srcKey");
            parameters.and("srcKey", "%" + srcKeyQuery.toLowerCase() + "%");
        }
        if (handshakeKeyQuery != null) {
            query.append(" AND (lower(handshakeKey)) LIKE :handshakeKey");
            parameters.and("handshakeKey", "%" + handshakeKeyQuery.toLowerCase() + "%");
        }
        if (encryptionKeyQuery != null) {
            query.append(" AND (lower(encryptionKey)) LIKE :encryptionKey");
            parameters.and("encryptionKey", "%" + encryptionKeyQuery.toLowerCase() + "%");
        }
        return executeFilter(query.toString(), parameters, pagination);
    }

    @WithTransaction
    @Override
    public Uni<Result<SessionEntity>> create(SessionEntity o) {
        boolean isUser = o.getUserOwner() != null;
        String password = isUser ? o.getUserOwner().getPassword() : o.getDeviceOwner().getPassword();
        Uni<?> entityUni = isUser ? userRepository.findById(o.getUserOwner().getId()) : deviceRepository.findById(o.getDeviceOwner().getId());
        return entityUni.chain(entity -> {
            if (entity == null) return Uni.createFrom().item(Result.notFound());
            String entityPassword = isUser ? ((UserEntity) (entity)).getPassword() : ((DeviceEntity) (entity)).getPassword();
            if (!password.equals(entityPassword)) return Uni.createFrom().item(Result.forbidden());
            if (isUser) {
                o.setDeviceOwner(null);
            } else {
                o.setUserOwner(null);
            }
            OffsetDateTime now = OffsetDateTime.now();
            o.setSrcKey(UUID.randomUUID());
            o.setHandshakeKey(o.getHandshakeKey());
            o.setEncryptionKey(SecurityHelper.generateXCC20Key());
            o.setLastUsed(now);
            o.setValidUntil(now.plusMinutes(sessionConfig.sessionDuration()));
            o.setCreatedAt(now);
            return super.create(o);
        });
    }

    @Override
    public Uni<Result<SessionEntity>> update(SessionEntity o) {
        return Uni.createFrom().item(Result.ok());
    }

    @Override
    public Uni<Result<List<SessionEntity>>> deleteById(List<UUID> ids) {
        UserEntity clientUser = SecurityHelper.securityIdentityUser(client);
        if (clientUser == null) return Uni.createFrom().item(Result.forbidden());
        boolean canSelfSessionEdit = clientUser.getRole().getAuthorities().contains(Authority.SELF_SESSION_DELETE);
        boolean canDeleteAnySession = clientUser.getRole().getAuthorities().contains(Authority.X_SESSION_DELETE);
        if (!new HashSet<>(clientUser.getSessions().stream().map(SessionEntity::getId).toList()).containsAll(ids) && !canSelfSessionEdit) return Uni.createFrom().item(Result.forbidden());
        String query = canDeleteAnySession ? "FROM SessionEntity s WHERE s.id IN :ids" : "FROM SessionEntity s WHERE s.id IN :ids AND" + (canSelfSessionEdit ? " (s.userOwner.role.grade < :clientGrade OR s.userOwner.id = :clientId)" : " s.userOwner.role.grade < :clientGrade");
        Parameters parameters = Parameters.with("ids", ids).and("clientGrade", clientUser.getRole().getGrade()).and("clientId", clientUser.getId());
        return sessionRepository.find(query, parameters.map()).list().chain(authorizedSessions -> {
            if (authorizedSessions.size() != ids.size()) return Uni.createFrom().item(Result.forbidden());
            return super.deleteById(ids);
        });
    }
}
