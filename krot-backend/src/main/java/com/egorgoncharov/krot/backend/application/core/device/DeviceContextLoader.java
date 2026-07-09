package com.egorgoncharov.krot.backend.application.core.device;

import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.application.context.common.EntityCreationContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityQueryContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityUpdateContext;
import com.egorgoncharov.krot.backend.application.context.loader.AbstractCrudContextLoader;
import com.egorgoncharov.krot.backend.application.query.filter.RangeFilter;
import com.egorgoncharov.krot.backend.database.relational.RelationalCrudRepository;
import com.egorgoncharov.krot.backend.database.relational.entity.DeviceCollaboratorEntity;
import com.egorgoncharov.krot.backend.database.relational.entity.DeviceEntity;
import com.egorgoncharov.krot.backend.database.relational.repository.DeviceRepository;
import com.egorgoncharov.krot.backend.database.relational.repository.UserRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@ApplicationScoped
public class DeviceContextLoader extends AbstractCrudContextLoader<DeviceEntity, UUID, Void, DeviceQuery> {
    private final DeviceRepository repository;
    private final UserRepository userRepository;

    @Inject
    public DeviceContextLoader(DeviceRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Override
    protected RelationalCrudRepository<DeviceEntity, UUID> repository() {
        return repository;
    }

    @Override
    public Uni<Result<? extends EntityQueryContext<DeviceEntity, UUID, Void>>> queryContext(DeviceQuery query, Void subcontext) {
        StringBuilder queryBuilder = new StringBuilder("1=1");
        Map<String, Object> parameters = new HashMap<>();
        if (query.getIds() != null && !query.getIds().isEmpty()) {
            queryBuilder.append(" AND id IN :ids");
            parameters.put("ids", query.getIds());
        }
        if (query.getOwnerId() != null) {
            queryBuilder.append(" AND owner.id = :ownerId");
            parameters.put("ownerId", query.getOwnerId().toString());
        }
        RangeFilter.applyRangeFilter(queryBuilder, "lastUpdated", parameters, query.getLastUpdateTime());
        RangeFilter.applyRangeFilter(queryBuilder, "createdAt", parameters, query.getCreationTime());
        if (query.getNameQuery() != null) {
            queryBuilder.append(" AND (lower(name)) LIKE :name");
            parameters.put("name", "%" + query.getNameQuery().toLowerCase() + "%");
        }
        if (query.getAddressQuery() != null) {
            queryBuilder.append(" AND (lower(address)) LIKE :address");
            parameters.put("address", "%" + query.getAddressQuery().toLowerCase() + "%");
        }
        return executeQuery(queryBuilder.toString(), parameters, query).map(result -> {
            if (result.getCode() != 200 || result.getResult().isEmpty()) return result;
            EntityQueryContext<DeviceEntity, UUID, Void> context = result.getResult().get();
            context.getResults().getItems().forEach(device -> {
                DeviceCollaboratorEntity principalAsCollaborator = device.getCollaborators().stream().filter(c -> c.getCollaborator().getId().equals(context.principal().getId())).findFirst().orElse(null);
                if (principalAsCollaborator != null) {
                    if (!principalAsCollaborator.getCanReadPassword()) device.setPassword(null);
                    if (!principalAsCollaborator.getCanReadAddress()) device.setAddress(null);
                    if (!principalAsCollaborator.getCanReadLastUpdate()) device.setLastCalled(null);
                }
            });
            return result;
        });
    }

    @Override
    public Uni<Result<? extends EntityCreationContext<DeviceEntity, UUID, Void>>> creationContext(DeviceEntity entity, Void subcontext) {
        return super.creationContext(entity, subcontext).chain(contextResult -> {
            if (contextResult.getCode() != 200 || contextResult.getResult().isEmpty()) return contextResult.toUni();
            EntityCreationContext<DeviceEntity, UUID, Void> context = contextResult.getResult().get();
            OffsetDateTime now = OffsetDateTime.now();
            context.getEntity().setId(null);
            context.getEntity().setAddress("0.0.0.0"); // TODO: Extract real request IP
            context.getEntity().setOwner(context.principal());
            context.getEntity().setLastCalled(now);
            context.getEntity().setCreatedAt(now);
            context.getEntity().setCollaborators(null);
            return contextResult.toUni();
        });
    }

    @Override
    public Uni<Result<? extends EntityUpdateContext<DeviceEntity, UUID, Void>>> updateContext(DeviceEntity entity, Void subcontext) {
        return super.updateContext(entity, subcontext).chain(contextResult -> {
            if (contextResult.getCode() != 200 || contextResult.getResult().isEmpty()) return contextResult.toUni();
            EntityUpdateContext<DeviceEntity, UUID, Void> context = contextResult.getResult().get();
            context.getNewEntity().setAddress(null);
            context.getNewEntity().setOwner(null);
            context.getNewEntity().setLastCalled(null);
            context.getNewEntity().setCreatedAt(null);
            context.getNewEntity().setCollaborators(null);
            return contextResult.toUni();
        });
    }

    public Uni<Result<? extends EntityUpdateContext<DeviceEntity, UUID, Void>>> collaboratorUpsertContext(DeviceEntity entity) {
        return collaboratorManageContext(entity).chain(contextResult -> {
            if (contextResult.getCode() != 200 || contextResult.getResult().isEmpty()) return contextResult.toUni();
            EntityUpdateContext<DeviceEntity, UUID, Void> context = contextResult.getResult().get();
            DeviceCollaboratorEntity targetCollaborator = context.getNewEntity().getCollaborators().getFirst();
            targetCollaborator.setDevice(entity);
            return userRepository.findById(targetCollaborator.getCollaborator().getId())
                    .map(Objects::nonNull)
                    .chain(exists -> exists ? contextResult.toUni() : Result.<EntityUpdateContext<DeviceEntity, UUID, Void>>notFound().toUni());
        });
    }

    public Uni<Result<? extends EntityUpdateContext<DeviceEntity, UUID, Void>>> collaboratorDeleteContext(DeviceEntity entity) {
        return collaboratorManageContext(entity);
    }

    public Uni<Result<? extends EntityUpdateContext<DeviceEntity, UUID, Void>>> ownershipTransferContext(DeviceEntity entity) {
        return super.updateContext(entity).chain(contextResult -> {
            if (contextResult.getCode() != 200 || contextResult.getResult().isEmpty()) return contextResult.toUni();
            EntityUpdateContext<DeviceEntity, UUID, Void> context = contextResult.getResult().get();
            context.getNewEntity().setName(null);
            context.getNewEntity().setAddress(null);
            context.getNewEntity().setPassword(null);
            context.getNewEntity().setLastCalled(null);
            context.getNewEntity().setCreatedAt(null);
            return userRepository.findById(context.getNewEntity().getOwner().getId())
                    .map(Objects::nonNull)
                    .chain(exists -> exists ? contextResult.toUni() : Result.<EntityUpdateContext<DeviceEntity, UUID, Void>>notFound().toUni());
        });
    }

    protected Uni<Result<? extends EntityUpdateContext<DeviceEntity, UUID, Void>>> collaboratorManageContext(DeviceEntity entity) {
        return super.updateContext(entity).map(contextResult -> {
            if (contextResult.getCode() != 200 || contextResult.getResult().isEmpty()) return contextResult;
            EntityUpdateContext<DeviceEntity, UUID, Void> context = contextResult.getResult().get();
            if (context.getNewEntity().getCollaborators().size() != 1) return Result.badRequest("Can create/update/delete only one collaborator at a time");
            context.getNewEntity().setName(null);
            context.getNewEntity().setAddress(null);
            context.getNewEntity().setPassword(null);
            context.getNewEntity().setOwner(null);
            context.getNewEntity().setLastCalled(null);
            context.getNewEntity().setCreatedAt(null);
            return contextResult;
        });
    }
}
