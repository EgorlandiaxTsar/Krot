package com.egorgoncharov.krot.backend.application.service;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.application.context.model.CreationContext;
import com.egorgoncharov.krot.backend.application.context.model.DeleteContext;
import com.egorgoncharov.krot.backend.application.context.model.UpdateContext;
import com.egorgoncharov.krot.backend.application.service.modification.AbstractModificationService;
import com.egorgoncharov.krot.backend.database.relational.RelationalCrudRepository;
import com.egorgoncharov.krot.backend.database.relational.entity.RoleEntity;
import com.egorgoncharov.krot.backend.database.relational.repository.RoleRepository;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.*;

@ApplicationScoped
public class RoleService extends AbstractModificationService<RoleEntity, UUID, Void> {
    private final RoleRepository repository;

    @Inject
    public RoleService(RoleRepository repository) {
        this.repository = repository;
    }

    @Override
    protected RelationalCrudRepository<RoleEntity, UUID> repository() {
        return repository;
    }

    @WithTransaction
    @Override
    public Uni<Result<RoleEntity>> create(CreationContext<RoleEntity, UUID, Void> context) {
        return repository.existsByName(context.getEntity().getName()).chain(exists -> exists ? Uni.createFrom().item(Result.conflict("name")) : super.create(context));
    }

    @WithTransaction
    @Override
    public Uni<Result<RoleEntity>> update(UpdateContext<RoleEntity, UUID, Void> context) {
        Uni<Boolean> nameExistsUni = (context.getNewEntity().getName() == null || context.getNewEntity().getName().equals(context.getOldEntity().getName())) ? Uni.createFrom().item(false) : repository.find("name = ?1 AND id != ?2", context.getNewEntity().getName(), context.getNewEntity().getId()).firstResult().map(Objects::nonNull);
        return nameExistsUni.chain(exists -> {
            if (exists) return Uni.createFrom().item(Result.conflict("name"));
            return super.update(context);
        });
    }

    @WithTransaction
    @Override
    public Uni<Result<List<RoleEntity>>> delete(DeleteContext<RoleEntity, UUID, Void> context) {
        Map<String, Object> parameters = new HashMap<>() {{
            put("ids", context.getEntities().stream().map(RoleEntity::getId).toList());
        }};
        return repository.find("FROM RoleEntity r WHERE r.id IN :ids AND u.users IS NOT EMPTY", parameters).firstResult().chain(relations -> {
            if (relations != null) return Uni.createFrom().item(Result.badRequest("Some roles contain users, make sure to assign new roles to current users and try again"));
            return super.delete(context);
        });
    }
}
