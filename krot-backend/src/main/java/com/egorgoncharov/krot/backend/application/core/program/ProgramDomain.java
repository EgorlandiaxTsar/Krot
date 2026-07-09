package com.egorgoncharov.krot.backend.application.core.program;

import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.application.context.loader.CrudContextLoader;
import com.egorgoncharov.krot.backend.application.domain.AbstractCrudDomain;
import com.egorgoncharov.krot.backend.application.evaluator.CrudPermissionsEvaluator;
import com.egorgoncharov.krot.backend.application.service.ModifyingService;
import com.egorgoncharov.krot.backend.database.relational.entity.ProgramEntity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class ProgramDomain extends AbstractCrudDomain<ProgramEntity, UUID, Void, ProgramQuery> {
    private final ProgramContextLoader contextLoader;
    private final ProgramPermissionsEvaluator permissionsEvaluator;
    private final ProgramService service;

    @Inject
    public ProgramDomain(ProgramContextLoader contextLoader, ProgramPermissionsEvaluator permissionsEvaluator, ProgramService service) {
        this.contextLoader = contextLoader;
        this.permissionsEvaluator = permissionsEvaluator;
        this.service = service;
    }

    @Override
    protected CrudContextLoader<ProgramEntity, UUID, Void, ProgramQuery> contextLoader() {
        return contextLoader;
    }

    @Override
    protected CrudPermissionsEvaluator<ProgramEntity, UUID, Void> permissionsEvaluator() {
        return permissionsEvaluator;
    }

    @Override
    protected ModifyingService<ProgramEntity, UUID, Void> service() {
        return service;
    }

    public Uni<Result<Void>> upsertCollaborator(ProgramEntity entity) {
        return executePipeline(contextLoader.collaboratorUpsertContext(entity), permissionsEvaluator::canManageCollaborators, service::upsertCollaborator);
    }

    public Uni<Result<Void>> deleteCollaborator(ProgramEntity entity) {
        return executePipeline(contextLoader.collaboratorDeleteContext(entity), permissionsEvaluator::canManageCollaborators, service::deleteCollaborator);
    }

    public Uni<Result<Void>> transferOwnership(ProgramEntity entity) {
        return executePipeline(contextLoader.ownershipTransferContext(entity), permissionsEvaluator::canTransferOwnership, service::transferOwnership);
    }
}
