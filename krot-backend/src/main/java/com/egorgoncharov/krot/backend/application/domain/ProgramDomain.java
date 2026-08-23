package com.egorgoncharov.krot.backend.application.domain;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.application.context.loader.CrudContextLoader;
import com.egorgoncharov.krot.backend.application.context.loader.ProgramContextLoader;
import com.egorgoncharov.krot.backend.application.events.bus.AbstractEventBus;
import com.egorgoncharov.krot.backend.application.events.bus.ProgramEventBus;
import com.egorgoncharov.krot.backend.application.guard.modification.ModificationGuard;
import com.egorgoncharov.krot.backend.application.guard.modification.ProgramModificationGuard;
import com.egorgoncharov.krot.backend.application.guard.view.ProgramViewGuard;
import com.egorgoncharov.krot.backend.application.guard.view.ViewGuard;
import com.egorgoncharov.krot.backend.application.query.model.ProgramQuery;
import com.egorgoncharov.krot.backend.application.service.ProgramService;
import com.egorgoncharov.krot.backend.application.service.modification.ModificationService;
import com.egorgoncharov.krot.backend.database.relational.entity.ProgramEntity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class ProgramDomain extends AbstractCrudDomain<ProgramEntity, UUID, Void, ProgramQuery> {
    private final ProgramContextLoader contextLoader;
    private final ProgramModificationGuard modificationGuard;
    private final ProgramViewGuard visibilityGuard;
    private final ProgramService service;
    private final ProgramEventBus eventBus;

    @Inject
    public ProgramDomain(ProgramContextLoader contextLoader, ProgramModificationGuard modificationGuard, ProgramViewGuard visibilityGuard, ProgramService service, ProgramEventBus eventBus) {
        this.contextLoader = contextLoader;
        this.modificationGuard = modificationGuard;
        this.visibilityGuard = visibilityGuard;
        this.service = service;
        this.eventBus = eventBus;
    }

    @Override
    protected CrudContextLoader<ProgramEntity, UUID, Void, ProgramQuery> contextLoader() {
        return contextLoader;
    }

    @Override
    protected ModificationGuard<ProgramEntity, UUID, Void> modificationGuard() {
        return modificationGuard;
    }

    @Override
    protected ViewGuard<ProgramEntity, UUID, Void> visibilityGuard() {
        return visibilityGuard;
    }

    @Override
    protected ModificationService<ProgramEntity, UUID, Void> service() {
        return service;
    }

    @Override
    protected AbstractEventBus<ProgramEntity, UUID, Void> eventBus() {
        return eventBus;
    }

    public Uni<Result<Void>> upsertCollaborator(ProgramEntity entity) {
        return executePipeline(contextLoader.collaboratorUpsertContext(entity), modificationGuard::canManageCollaborators, service::upsertCollaborator);
    }

    public Uni<Result<Void>> deleteCollaborator(ProgramEntity entity) {
        return executePipeline(contextLoader.collaboratorDeleteContext(entity), modificationGuard::canManageCollaborators, service::deleteCollaborator);
    }

    public Uni<Result<Void>> transferOwnership(ProgramEntity entity) {
        return executePipeline(contextLoader.ownershipTransferContext(entity), modificationGuard::canTransferOwnership, service::transferOwnership);
    }
}
