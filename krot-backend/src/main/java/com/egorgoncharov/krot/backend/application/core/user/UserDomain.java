package com.egorgoncharov.krot.backend.application.core.user;

import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.application.context.loader.CrudContextLoader;
import com.egorgoncharov.krot.backend.application.domain.AbstractCrudDomain;
import com.egorgoncharov.krot.backend.application.evaluator.CrudPermissionsEvaluator;
import com.egorgoncharov.krot.backend.application.service.ModifyingService;
import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class UserDomain extends AbstractCrudDomain<UserEntity, UUID, Void, UserQuery> {
    private final UserContextLoader contextLoader;
    private final UserPermissionsEvaluator permissionsEvaluator;
    private final UserService service;

    @Inject
    public UserDomain(UserContextLoader contextLoader, UserPermissionsEvaluator permissionsEvaluator, UserService service) {
        this.contextLoader = contextLoader;
        this.permissionsEvaluator = permissionsEvaluator;
        this.service = service;
    }

    @Override
    protected CrudContextLoader<UserEntity, UUID, Void, UserQuery> contextLoader() {
        return contextLoader;
    }

    @Override
    protected CrudPermissionsEvaluator<UserEntity, UUID, Void> permissionsEvaluator() {
        return permissionsEvaluator;
    }

    @Override
    protected ModifyingService<UserEntity, UUID, Void> service() {
        return service;
    }

    public Uni<Result<Void>> updatePassword(UserEntity entity, String oldPassword) {
        return executePipeline(contextLoader.updatePasswordContext(entity, oldPassword), permissionsEvaluator::canUpdatePassword, service::updatePassword);
    }
}
