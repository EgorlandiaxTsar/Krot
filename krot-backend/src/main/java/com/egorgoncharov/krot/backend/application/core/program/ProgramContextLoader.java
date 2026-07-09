package com.egorgoncharov.krot.backend.application.core.program;

import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.application.context.common.EntityCreationContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityQueryContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityUpdateContext;
import com.egorgoncharov.krot.backend.application.context.loader.AbstractCrudContextLoader;
import com.egorgoncharov.krot.backend.application.query.filter.RangeFilter;
import com.egorgoncharov.krot.backend.database.relational.RelationalCrudRepository;
import com.egorgoncharov.krot.backend.database.relational.entity.ProgramCollaboratorEntity;
import com.egorgoncharov.krot.backend.database.relational.entity.ProgramEntity;
import com.egorgoncharov.krot.backend.database.relational.repository.ProgramRepository;
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
public class ProgramContextLoader extends AbstractCrudContextLoader<ProgramEntity, UUID, Void, ProgramQuery> {
    private final ProgramRepository repository;
    private final UserRepository userRepository;

    @Inject
    public ProgramContextLoader(ProgramRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Override
    protected RelationalCrudRepository<ProgramEntity, UUID> repository() {
        return repository;
    }

    @Override
    public Uni<Result<? extends EntityQueryContext<ProgramEntity, UUID, Void>>> queryContext(ProgramQuery query, Void subcontext) {
        StringBuilder queryBuilder = new StringBuilder("1=1");
        Map<String, Object> parameters = new HashMap<>();
        if (query.getIds() != null && !query.getIds().isEmpty()) {
            queryBuilder.append(" AND id IN :ids");
            parameters.put("ids", query.getIds());
        }
        RangeFilter.applyRangeFilter(queryBuilder, "creationTime", parameters, query.getCreationTime());
        if (query.getOwnerId() != null) {
            queryBuilder.append(" AND ownerId = :ownerId");
            parameters.put("ownerId", query.getOwnerId());
        }
        if (query.getNameQuery() != null) {
            queryBuilder.append(" AND (lower(name)) LIKE :name");
            parameters.put("name", "%" + query.getNameQuery().toLowerCase() + "%");
        }
        return executeQuery(queryBuilder.toString(), parameters, query);
    }

    @Override
    public Uni<Result<? extends EntityCreationContext<ProgramEntity, UUID, Void>>> creationContext(ProgramEntity entity, Void subcontext) {
        return super.creationContext(entity, subcontext).chain(contextResult -> {
            if (contextResult.getCode() != 200 || contextResult.getResult().isEmpty()) return contextResult.toUni();
            EntityCreationContext<ProgramEntity, UUID, Void> context = contextResult.getResult().get();
            context.getEntity().setId(null);
            context.getEntity().setOwner(context.principal());
            context.getEntity().setCreatedAt(OffsetDateTime.now());
            context.getEntity().setCollaborators(null);
            return contextResult.toUni();
        });
    }

    @Override
    public Uni<Result<? extends EntityUpdateContext<ProgramEntity, UUID, Void>>> updateContext(ProgramEntity entity, Void subcontext) {
        return super.updateContext(entity, subcontext).chain(contextResult -> {
            if (contextResult.getCode() != 200 || contextResult.getResult().isEmpty()) return contextResult.toUni();
            EntityUpdateContext<ProgramEntity, UUID, Void> context = contextResult.getResult().get();
            context.getNewEntity().setOwner(null);
            context.getNewEntity().setCreatedAt(null);
            context.getNewEntity().setCollaborators(null);
            return contextResult.toUni();
        });
    }

    public Uni<Result<? extends EntityUpdateContext<ProgramEntity, UUID, Void>>> collaboratorUpsertContext(ProgramEntity entity) {
        return collaboratorManageContext(entity).chain(contextResult -> {
            if (contextResult.getCode() != 200 || contextResult.getResult().isEmpty()) return contextResult.toUni();
            EntityUpdateContext<ProgramEntity, UUID, Void> context = contextResult.getResult().get();
            ProgramCollaboratorEntity targetCollaborator = context.getNewEntity().getCollaborators().getFirst();
            targetCollaborator.setProgram(entity);
            return userRepository.findById(targetCollaborator.getCollaborator().getId())
                    .map(Objects::nonNull)
                    .chain(exists -> exists ? contextResult.toUni() : Result.<EntityUpdateContext<ProgramEntity, UUID, Void>>notFound().toUni());
        });
    }

    public Uni<Result<? extends EntityUpdateContext<ProgramEntity, UUID, Void>>> collaboratorDeleteContext(ProgramEntity entity) {
        return collaboratorManageContext(entity);
    }

    public Uni<Result<? extends EntityUpdateContext<ProgramEntity, UUID, Void>>> ownershipTransferContext(ProgramEntity entity) {
        return super.updateContext(entity).chain(contextResult -> {
            if (contextResult.getCode() != 200 || contextResult.getResult().isEmpty()) return contextResult.toUni();
            EntityUpdateContext<ProgramEntity, UUID, Void> context = contextResult.getResult().get();
            context.getNewEntity().setName(null);
            context.getNewEntity().setCreatedAt(null);
            return userRepository.findById(context.getNewEntity().getOwner().getId())
                    .map(Objects::nonNull)
                    .chain(exists -> exists ? contextResult.toUni() : Result.<EntityUpdateContext<ProgramEntity, UUID, Void>>notFound().toUni());
        });
    }

    protected Uni<Result<? extends EntityUpdateContext<ProgramEntity, UUID, Void>>> collaboratorManageContext(ProgramEntity entity) {
        return super.updateContext(entity).map(contextResult -> {
            if (contextResult.getCode() != 200 || contextResult.getResult().isEmpty()) return contextResult;
            EntityUpdateContext<ProgramEntity, UUID, Void> context = contextResult.getResult().get();
            if (context.getNewEntity().getCollaborators().size() != 1) return Result.badRequest("Can create/update/delete only one collaborator at a time");
            context.getNewEntity().setName(null);
            context.getNewEntity().setOwner(null);
            context.getNewEntity().setCreatedAt(null);
            return contextResult;
        });
    }
}
