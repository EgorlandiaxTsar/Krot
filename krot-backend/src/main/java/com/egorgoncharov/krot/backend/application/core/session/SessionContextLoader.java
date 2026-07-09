package com.egorgoncharov.krot.backend.application.core.session;

import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.application.context.common.EntityCreationContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityDeleteContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityQueryContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityUpdateContext;
import com.egorgoncharov.krot.backend.application.context.loader.AbstractCrudContextLoader;
import com.egorgoncharov.krot.backend.application.query.filter.RangeFilter;
import com.egorgoncharov.krot.backend.database.Identifiable;
import com.egorgoncharov.krot.backend.database.relational.RelationalCrudRepository;
import com.egorgoncharov.krot.backend.database.relational.entity.DeviceEntity;
import com.egorgoncharov.krot.backend.database.relational.entity.HistoricalSessionEntity;
import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
import com.egorgoncharov.krot.backend.database.relational.repository.DeviceRepository;
import com.egorgoncharov.krot.backend.database.relational.repository.HistoricalSessionRepository;
import com.egorgoncharov.krot.backend.database.relational.repository.UserRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class SessionContextLoader extends AbstractCrudContextLoader<HistoricalSessionEntity, UUID, Void, SessionQuery> {
    private final HistoricalSessionRepository repository;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;

    @Inject
    public SessionContextLoader(HistoricalSessionRepository repository, UserRepository userRepository, DeviceRepository deviceRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
    }

    @Override
    protected RelationalCrudRepository<HistoricalSessionEntity, UUID> repository() {
        return repository;
    }

    @Override
    public Uni<Result<? extends EntityQueryContext<HistoricalSessionEntity, UUID, Void>>> queryContext(SessionQuery query, Void subcontext) {
        StringBuilder queryBuilder = new StringBuilder("1=1");
        Map<String, Object> parameters = new HashMap<>();
        if (query.getIds() != null && !query.getIds().isEmpty()) {
            queryBuilder.append(" AND id IN :ids");
            parameters.put("ids", query.getIds());
        }
        if (query.getOwnerId() != null) {
            queryBuilder.append(" AND (userOwner.id = :userOwnerId");
            parameters.put("userOwnerId", query.getOwnerId());
        }
        if (query.getOwnerId() != null) {
            queryBuilder.append(" OR deviceOwner.id = :deviceOwnerId");
            parameters.put("deviceOwnerId", query.getOwnerId());
        }
        queryBuilder.append(")");
        RangeFilter.applyRangeFilter(queryBuilder, "validUntil", parameters, query.getExpirationTime());
        RangeFilter.applyRangeFilter(queryBuilder, "createdAt", parameters, query.getCreationTime());
        return executeQuery(queryBuilder.toString(), parameters, query);
    }

    @Override
    public Uni<Result<? extends EntityCreationContext<HistoricalSessionEntity, UUID, Void>>> creationContext(HistoricalSessionEntity entity, Void subcontext) {
        return super.creationContext(entity, subcontext).chain(contextResult -> {
            if (contextResult.getCode() != 200 || contextResult.getResult().isEmpty()) return contextResult.toUni();
            EntityCreationContext<HistoricalSessionEntity, UUID, Void> context = contextResult.getResult().get();
            OffsetDateTime now = OffsetDateTime.now();
            context.getEntity().setId(null);
            context.getEntity().setValidUntil(now);
            context.getEntity().setCreatedAt(now);
            boolean isUser = context.getEntity().getUserOwner() != null;
            String password = isUser ? context.getEntity().getUserOwner().getPassword() : context.getEntity().getDeviceOwner().getPassword();
            Uni<? extends Identifiable<UUID>> entityUni = isUser ? userRepository.findByName(context.getEntity().getUserOwner().getUsername()) : deviceRepository.findById(context.getEntity().getDeviceOwner().getId());
            return entityUni.chain(owner -> {
                if (owner == null) return Result.<EntityCreationContext<HistoricalSessionEntity, UUID, Void>>notFound().toUni();
                String entityPassword = isUser ? ((UserEntity) (owner)).getPassword() : ((DeviceEntity) (owner)).getPassword();
                if (!password.equals(entityPassword)) return Result.<EntityCreationContext<HistoricalSessionEntity, UUID, Void>>forbidden().toUni();
                if (isUser) {
                    context.getEntity().setDeviceOwner(null);
                    context.getEntity().setUserOwner((UserEntity) owner);
                } else {
                    context.getEntity().setUserOwner(null);
                    context.getEntity().setDeviceOwner((DeviceEntity) owner);
                }
                return contextResult.toUni();
            });
        });
    }

    @Override
    public Uni<Result<? extends EntityUpdateContext<HistoricalSessionEntity, UUID, Void>>> updateContext(HistoricalSessionEntity entity, Void subcontext) {
        return null;
    }

    @Override
    public Uni<Result<? extends EntityDeleteContext<HistoricalSessionEntity, UUID, Void>>> deleteContext(List<HistoricalSessionEntity> entities, Void subcontext) {
        return null;
    }
}
