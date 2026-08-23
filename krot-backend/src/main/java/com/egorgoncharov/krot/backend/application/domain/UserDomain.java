package com.egorgoncharov.krot.backend.application.domain;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.application.context.loader.CrudContextLoader;
import com.egorgoncharov.krot.backend.application.context.loader.UserContextLoader;
import com.egorgoncharov.krot.backend.application.events.bus.AbstractEventBus;
import com.egorgoncharov.krot.backend.application.events.bus.UserEventBus;
import com.egorgoncharov.krot.backend.application.guard.modification.ModificationGuard;
import com.egorgoncharov.krot.backend.application.guard.modification.UserModificationGuard;
import com.egorgoncharov.krot.backend.application.guard.view.UserViewGuard;
import com.egorgoncharov.krot.backend.application.guard.view.ViewGuard;
import com.egorgoncharov.krot.backend.application.query.model.UserQuery;
import com.egorgoncharov.krot.backend.application.service.UserService;
import com.egorgoncharov.krot.backend.application.service.modification.ModificationService;
import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class UserDomain extends AbstractCrudDomain<UserEntity, UUID, Void, UserQuery> {
    private final UserContextLoader contextLoader;
    private final UserModificationGuard modificationGuard;
    private final UserViewGuard visibilityGuard;
    private final UserService service;
    private final UserEventBus eventBus;

    @Inject
    public UserDomain(UserContextLoader contextLoader, UserModificationGuard modificationGuard, UserViewGuard visibilityGuard, UserService service, UserEventBus eventBus) {
        this.contextLoader = contextLoader;
        this.modificationGuard = modificationGuard;
        this.visibilityGuard = visibilityGuard;
        this.service = service;
        this.eventBus = eventBus;
    }

    @Override
    protected CrudContextLoader<UserEntity, UUID, Void, UserQuery> contextLoader() {
        return contextLoader;
    }

    @Override
    protected ModificationGuard<UserEntity, UUID, Void> modificationGuard() {
        return modificationGuard;
    }

    @Override
    protected ViewGuard<UserEntity, UUID, Void> visibilityGuard() {
        return visibilityGuard;
    }

    @Override
    protected ModificationService<UserEntity, UUID, Void> service() {
        return service;
    }

    @Override
    protected AbstractEventBus<UserEntity, UUID, Void> eventBus() {
        return eventBus;
    }

    public Uni<Result<Void>> updatePassword(UserEntity entity, String oldPassword) {
        return executePipeline(contextLoader.updatePasswordContext(entity, oldPassword), modificationGuard::canUpdatePassword, service::updatePassword);
    }
}
