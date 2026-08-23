package com.egorgoncharov.krot.backend.application.domain;

import com.egorgoncharov.krot.backend.application.context.loader.CrudContextLoader;
import com.egorgoncharov.krot.backend.application.context.loader.RoleContextLoader;
import com.egorgoncharov.krot.backend.application.events.bus.AbstractEventBus;
import com.egorgoncharov.krot.backend.application.events.bus.RoleEventBus;
import com.egorgoncharov.krot.backend.application.guard.modification.ModificationGuard;
import com.egorgoncharov.krot.backend.application.guard.modification.RoleModificationGuard;
import com.egorgoncharov.krot.backend.application.guard.view.RoleViewGuard;
import com.egorgoncharov.krot.backend.application.guard.view.ViewGuard;
import com.egorgoncharov.krot.backend.application.query.model.RoleQuery;
import com.egorgoncharov.krot.backend.application.service.RoleService;
import com.egorgoncharov.krot.backend.application.service.modification.ModificationService;
import com.egorgoncharov.krot.backend.database.relational.entity.RoleEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class RoleDomain extends AbstractCrudDomain<RoleEntity, UUID, Void, RoleQuery> {
    private final RoleContextLoader contextLoader;
    private final RoleModificationGuard modificationGuard;
    private final RoleViewGuard visibilityGuard;
    private final RoleService service;
    private final RoleEventBus eventBus;

    @Inject
    public RoleDomain(RoleContextLoader contextLoader, RoleModificationGuard modificationGuard, RoleViewGuard visibilityGuard, RoleService service, RoleEventBus eventBus) {
        this.contextLoader = contextLoader;
        this.modificationGuard = modificationGuard;
        this.visibilityGuard = visibilityGuard;
        this.service = service;
        this.eventBus = eventBus;
    }

    @Override
    protected CrudContextLoader<RoleEntity, UUID, Void, RoleQuery> contextLoader() {
        return contextLoader;
    }

    @Override
    protected ModificationGuard<RoleEntity, UUID, Void> modificationGuard() {
        return modificationGuard;
    }

    @Override
    protected ViewGuard<RoleEntity, UUID, Void> visibilityGuard() {
        return visibilityGuard;
    }

    @Override
    protected ModificationService<RoleEntity, UUID, Void> service() {
        return service;
    }

    @Override
    protected AbstractEventBus<RoleEntity, UUID, Void> eventBus() {
        return eventBus;
    }
}
