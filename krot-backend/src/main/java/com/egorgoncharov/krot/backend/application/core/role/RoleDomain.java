package com.egorgoncharov.krot.backend.application.core.role;

import com.egorgoncharov.krot.backend.application.context.loader.CrudContextLoader;
import com.egorgoncharov.krot.backend.application.domain.AbstractCrudDomain;
import com.egorgoncharov.krot.backend.application.evaluator.CrudPermissionsEvaluator;
import com.egorgoncharov.krot.backend.application.service.ModifyingService;
import com.egorgoncharov.krot.backend.database.relational.entity.RoleEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class RoleDomain extends AbstractCrudDomain<RoleEntity, UUID, Void, RoleQuery> {
    private final RoleContextLoader contextLoader;
    private final RolePermissionsEvaluator permissionsEvaluator;
    private final RoleService service;

    @Inject
    public RoleDomain(RoleContextLoader contextLoader, RolePermissionsEvaluator permissionsEvaluator, RoleService service) {
        this.contextLoader = contextLoader;
        this.permissionsEvaluator = permissionsEvaluator;
        this.service = service;
    }

    @Override
    protected CrudContextLoader<RoleEntity, UUID, Void, RoleQuery> contextLoader() {
        return contextLoader;
    }

    @Override
    protected CrudPermissionsEvaluator<RoleEntity, UUID, Void> permissionsEvaluator() {
        return permissionsEvaluator;
    }

    @Override
    protected ModifyingService<RoleEntity, UUID, Void> service() {
        return service;
    }
}
