package com.egorgoncharov.krot.backend.application.core.device;

import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.application.context.loader.CrudContextLoader;
import com.egorgoncharov.krot.backend.application.domain.AbstractCrudDomain;
import com.egorgoncharov.krot.backend.application.evaluator.CrudPermissionsEvaluator;
import com.egorgoncharov.krot.backend.application.service.ModifyingService;
import com.egorgoncharov.krot.backend.database.relational.entity.DeviceEntity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class DeviceDomain extends AbstractCrudDomain<DeviceEntity, UUID, Void, DeviceQuery> {
    private final DeviceContextLoader contextLoader;
    private final DevicePermissionsEvaluator permissionsEvaluator;
    private final DeviceService service;

    @Inject
    public DeviceDomain(DeviceContextLoader contextLoader, DevicePermissionsEvaluator permissionsEvaluator, DeviceService service) {
        this.contextLoader = contextLoader;
        this.permissionsEvaluator = permissionsEvaluator;
        this.service = service;
    }

    @Override
    protected CrudContextLoader<DeviceEntity, UUID, Void, DeviceQuery> contextLoader() {
        return contextLoader;
    }

    @Override
    protected CrudPermissionsEvaluator<DeviceEntity, UUID, Void> permissionsEvaluator() {
        return permissionsEvaluator;
    }

    @Override
    protected ModifyingService<DeviceEntity, UUID, Void> service() {
        return service;
    }

    public Uni<Result<Void>> upsertCollaborator(DeviceEntity entity) {
        return executePipeline(contextLoader.collaboratorUpsertContext(entity), permissionsEvaluator::canManageCollaborators, service::upsertCollaborator);
    }

    public Uni<Result<Void>> deleteCollaborator(DeviceEntity entity) {
        return executePipeline(contextLoader.collaboratorDeleteContext(entity), permissionsEvaluator::canManageCollaborators, service::deleteCollaborator);
    }

    public Uni<Result<Void>> transferOwnership(DeviceEntity entity) {
        return executePipeline(contextLoader.ownershipTransferContext(entity), permissionsEvaluator::canTransferOwnership, service::transferOwnership);
    }
}
