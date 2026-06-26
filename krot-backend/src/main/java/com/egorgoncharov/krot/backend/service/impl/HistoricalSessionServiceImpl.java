package com.egorgoncharov.krot.backend.service.impl;

import com.egorgoncharov.krot.backend.config.yaml.SessionConfig;
import com.egorgoncharov.krot.backend.dto.service.Result;
import com.egorgoncharov.krot.backend.dto.service.filter.RangeFilter;
import com.egorgoncharov.krot.backend.dto.service.filter.TimeRangeFilter;
import com.egorgoncharov.krot.backend.dto.service.pagination.Page;
import com.egorgoncharov.krot.backend.dto.service.pagination.PaginationOptions;
import com.egorgoncharov.krot.backend.model.relational.RelationalReactiveRepository;
import com.egorgoncharov.krot.backend.model.relational.entity.DeviceEntity;
import com.egorgoncharov.krot.backend.model.relational.entity.HistoricalSessionEntity;
import com.egorgoncharov.krot.backend.model.relational.entity.UserEntity;
import com.egorgoncharov.krot.backend.model.relational.repository.DeviceRepository;
import com.egorgoncharov.krot.backend.model.relational.repository.HistoricalSessionRepository;
import com.egorgoncharov.krot.backend.model.relational.repository.UserRepository;
import com.egorgoncharov.krot.backend.security.Authority;
import com.egorgoncharov.krot.backend.service.HistoricalSessionService;
import com.egorgoncharov.krot.backend.service.common.ReactiveCrudService;
import com.egorgoncharov.krot.backend.service.helper.SecurityHelper;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class HistoricalSessionServiceImpl extends ReactiveCrudService<HistoricalSessionEntity, UUID> implements HistoricalSessionService {
    private final SecurityIdentity client;
    private final SessionConfig sessionConfig;
    private final HistoricalSessionRepository historicalSessionRepository;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;

    @Inject
    public HistoricalSessionServiceImpl(SecurityIdentity client, SessionConfig sessionConfig, HistoricalSessionRepository historicalSessionRepository, UserRepository userRepository, DeviceRepository deviceRepository) {
        this.client = client;
        this.sessionConfig = sessionConfig;
        this.historicalSessionRepository = historicalSessionRepository;
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
    }

    @Override
    protected RelationalReactiveRepository<HistoricalSessionEntity, UUID> repository() {
        return historicalSessionRepository;
    }

    @Override
    public Uni<Result<Page<HistoricalSessionEntity>>> filter(List<UUID> ids, UUID userOwnerId, UUID deviceOwnerId, TimeRangeFilter validUntilTime, TimeRangeFilter creationTime, PaginationOptions pagination) {
        UserEntity clientUser = SecurityHelper.securityIdentityUser(client);
        if (clientUser == null) return Uni.createFrom().item(Result.forbidden());
        StringBuilder query = new StringBuilder("1=1");
        Map<String, Object> parameters = new HashMap<>();
        if (!clientUser.getRole().getAuthorities().contains(Authority.X_SESSION_READ)) {
            if (clientUser.getRole().getAuthorities().contains(Authority.SESSION_READ)) {
                query.append(" AND session.userOwner.role.grade <= :clientGrade");
                parameters.put("clientGrade", clientUser.getRole().getGrade());
            } else if (clientUser.getRole().getAuthorities().contains(Authority.SELF_READ)) {
                query.append(" AND session.userOwner.id = :clientId");
                parameters.put("clientId", clientUser.getId());
                return executeFilter(query.toString(), parameters, pagination);
            } else {
                return Uni.createFrom().item(Result.forbidden());
            }
        }
        if (ids != null && !ids.isEmpty()) {
            query.append(" AND id IN :ids");
            parameters.put("ids", ids);
        }
        if (userOwnerId != null) {
            query.append(" AND (userOwner.id = :userOwnerId");
            parameters.put("userOwnerId", userOwnerId);
        }
        if (deviceOwnerId != null) {
            query.append(" OR deviceOwner.id = :deviceOwnerId");
            parameters.put("deviceOwnerId", deviceOwnerId);
        }
        query.append(")");
        RangeFilter.applyRangeFilter(query, "validUntil", parameters, validUntilTime);
        RangeFilter.applyRangeFilter(query, "createdAt", parameters, creationTime);
        return executeFilter(query.toString(), parameters, pagination);
    }

    @WithTransaction
    @Override
    public Uni<Result<HistoricalSessionEntity>> create(HistoricalSessionEntity o) {
        boolean isUser = o.getUserOwner() != null;
        String password = isUser ? o.getUserOwner().getPassword() : o.getDeviceOwner().getPassword();
        Uni<?> entityUni = isUser ? userRepository.findByName(o.getUserOwner().getUsername()) : deviceRepository.findById(o.getDeviceOwner().getId());
        return entityUni.chain(entity -> {
            if (entity == null) return Uni.createFrom().item(Result.notFound());
            String entityPassword = isUser ? ((UserEntity) (entity)).getPassword() : ((DeviceEntity) (entity)).getPassword();
            if (!password.equals(entityPassword)) return Uni.createFrom().item(Result.forbidden());
            if (isUser) {
                o.setDeviceOwner(null);
                o.setUserOwner((UserEntity) entity);
            } else {
                o.setUserOwner(null);
                o.setDeviceOwner((DeviceEntity) entity);
            }
            OffsetDateTime now = OffsetDateTime.now();
            o.setValidUntil(now.plusMinutes(sessionConfig.sessionDuration()));
            o.setCreatedAt(now);
            return super.create(o);
        });
    }

    @WithTransaction
    @Override
    public Uni<Result<HistoricalSessionEntity>> update(HistoricalSessionEntity o) {
        return Uni.createFrom().item(Result.badRequest()); // Immutable
    }

    @WithTransaction
    @Override
    public Uni<Result<List<HistoricalSessionEntity>>> deleteById(List<UUID> ids) {
        return Uni.createFrom().item(Result.badRequest()); // Persistent
    }
}
