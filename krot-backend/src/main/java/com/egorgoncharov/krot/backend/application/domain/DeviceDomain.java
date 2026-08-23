package com.egorgoncharov.krot.backend.application.domain;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.application.context.loader.CrudContextLoader;
import com.egorgoncharov.krot.backend.application.context.loader.DeviceContextLoader;
import com.egorgoncharov.krot.backend.application.events.bus.AbstractEventBus;
import com.egorgoncharov.krot.backend.application.events.bus.DeviceEventBus;
import com.egorgoncharov.krot.backend.application.guard.modification.DeviceModificationGuard;
import com.egorgoncharov.krot.backend.application.guard.modification.ModificationGuard;
import com.egorgoncharov.krot.backend.application.guard.view.DeviceViewGuard;
import com.egorgoncharov.krot.backend.application.guard.view.ViewGuard;
import com.egorgoncharov.krot.backend.application.query.model.DeviceQuery;
import com.egorgoncharov.krot.backend.application.service.DeviceService;
import com.egorgoncharov.krot.backend.application.service.modification.ModificationService;
import com.egorgoncharov.krot.backend.database.relational.entity.DeviceEntity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class DeviceDomain extends AbstractCrudDomain<DeviceEntity, UUID, Void, DeviceQuery> {
    private final DeviceContextLoader contextLoader;
    private final DeviceModificationGuard modificationGuard;
    private final DeviceViewGuard visibilityGuard;
    private final DeviceService service;
    private final DeviceEventBus eventBus;

    @Inject
    public DeviceDomain(DeviceContextLoader contextLoader, DeviceModificationGuard modificationGuard, DeviceViewGuard visibilityGuard, DeviceService service, DeviceEventBus eventBus) {
        this.contextLoader = contextLoader;
        this.modificationGuard = modificationGuard;
        this.visibilityGuard = visibilityGuard;
        this.service = service;
        this.eventBus = eventBus;
    }

    @Override
    protected CrudContextLoader<DeviceEntity, UUID, Void, DeviceQuery> contextLoader() {
        return contextLoader;
    }

    @Override
    protected ModificationGuard<DeviceEntity, UUID, Void> modificationGuard() {
        return modificationGuard;
    }

    @Override
    protected ViewGuard<DeviceEntity, UUID, Void> visibilityGuard() {
        return visibilityGuard;
    }

    @Override
    protected ModificationService<DeviceEntity, UUID, Void> service() {
        return service;
    }

    @Override
    protected AbstractEventBus<DeviceEntity, UUID, Void> eventBus() {
        return eventBus;
    }

    public Uni<Result<Void>> upsertCollaborator(DeviceEntity entity) {
        return executePipeline(contextLoader.collaboratorUpsertContext(entity), modificationGuard::canManageCollaborators, service::upsertCollaborator);
    }

    public Uni<Result<Void>> deleteCollaborator(DeviceEntity entity) {
        return executePipeline(contextLoader.collaboratorDeleteContext(entity), modificationGuard::canManageCollaborators, service::deleteCollaborator);
    }

    public Uni<Result<Void>> transferOwnership(DeviceEntity entity) {
        return executePipeline(contextLoader.ownershipTransferContext(entity), modificationGuard::canTransferOwnership, service::transferOwnership);
    }
}
