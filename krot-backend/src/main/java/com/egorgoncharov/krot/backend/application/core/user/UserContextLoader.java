package com.egorgoncharov.krot.backend.application.core.user;

import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.application.context.common.EntityCreationContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityQueryContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityUpdateContext;
import com.egorgoncharov.krot.backend.application.context.loader.AbstractCrudContextLoader;
import com.egorgoncharov.krot.backend.application.query.filter.RangeFilter;
import com.egorgoncharov.krot.backend.database.relational.RelationalCrudRepository;
import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
import com.egorgoncharov.krot.backend.database.relational.repository.RoleRepository;
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
public class UserContextLoader extends AbstractCrudContextLoader<UserEntity, UUID, Void, UserQuery> {
    private final UserRepository repository;
    private final RoleRepository roleRepository;

    @Inject
    public UserContextLoader(UserRepository repository, RoleRepository roleRepository) {
        this.repository = repository;
        this.roleRepository = roleRepository;
    }

    @Override
    protected RelationalCrudRepository<UserEntity, UUID> repository() {
        return repository;
    }

    @Override
    public Uni<Result<? extends EntityQueryContext<UserEntity, UUID, Void>>> queryContext(UserQuery query, Void subcontext) {
        StringBuilder queryBuilder = new StringBuilder("1=1");
        Map<String, Object> parameters = new HashMap<>();
        if (query.getIds() != null && !query.getIds().isEmpty()) {
            queryBuilder.append(" AND id IN :ids");
            parameters.put("ids", query.getIds());
        }
        if (query.getActive() != null) {
            queryBuilder.append(" AND active = :active");
            parameters.put("active", query.getActive());
        }
        RangeFilter.applyRangeFilter(queryBuilder, "createdAt", parameters, query.getCreationTime());
        if (query.getRoleId() != null) {
            queryBuilder.append(" AND role.id = :roleId");
            parameters.put("roleId", query.getRoleId().toString());
        }
        if (query.getUsernameQuery() != null) {
            queryBuilder.append(" AND (lower(username)) LIKE :username");
            parameters.put("username", "%" + query.getUsernameQuery().toLowerCase() + "%");
        }
        return executeQuery(queryBuilder.toString(), parameters, query);
    }

    @Override
    public Uni<Result<? extends EntityCreationContext<UserEntity, UUID, Void>>> creationContext(UserEntity entity, Void subcontext) {
        return super.creationContext(entity, subcontext).chain(contextResult -> {
            if (contextResult.getCode() != 200 || contextResult.getResult().isEmpty()) return contextResult.toUni();
            EntityCreationContext<UserEntity, UUID, Void> context = contextResult.getResult().get();
            context.getEntity().setId(null);
            context.getEntity().setActive(true);
            context.getEntity().setCreatedAt(OffsetDateTime.now());
            return roleRepository.findById(context.getEntity().getRole().getId())
                    .map(Objects::nonNull)
                    .chain(exists -> exists ? contextResult.toUni() : Result.<EntityCreationContext<UserEntity, UUID, Void>>notFound().toUni());
        });
    }

    @Override
    public Uni<Result<? extends EntityUpdateContext<UserEntity, UUID, Void>>> updateContext(UserEntity entity, Void subcontext) {
        return super.updateContext(entity, subcontext).chain(contextResult -> {
            if (contextResult.getCode() != 200 || contextResult.getResult().isEmpty()) return contextResult.toUni();
            EntityUpdateContext<UserEntity, UUID, Void> context = contextResult.getResult().get();
            context.getNewEntity().setPassword(null);
            context.getNewEntity().setCreatedAt(null);
            Uni<Boolean> roleExistsUni = (context.getNewEntity().getRole() == null || context.getNewEntity().getRole().getId().equals(context.getOldEntity().getRole().getId())) ? Uni.createFrom().item(true) : roleRepository.findById(context.getNewEntity().getRole().getId()).map(Objects::nonNull);
            return roleExistsUni.chain(exists -> exists ? contextResult.toUni() : Result.<EntityUpdateContext<UserEntity, UUID, Void>>notFound().toUni());
        });
    }

    public Uni<Result<? extends EntityUpdateContext<UserEntity, UUID, Void>>> updatePasswordContext(UserEntity entity, String oldPassword) {
        return super.updateContext(entity).map(contextResult -> {
            if (contextResult.getCode() != 200 || contextResult.getResult().isEmpty()) return contextResult;
            EntityUpdateContext<UserEntity, UUID, Void> context = contextResult.getResult().get();
            context.getOldEntity().setPassword(oldPassword);
            context.getNewEntity().setUsername(null);
            context.getNewEntity().setPassword(entity.getPassword());
            context.getNewEntity().setRole(null);
            context.getNewEntity().setActive(null);
            context.getNewEntity().setCreatedAt(null);
            return contextResult;
        });
    }
}
