package com.egorgoncharov.krot.backend.application.domain;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.application.context.loader.CrudContextLoader;
import com.egorgoncharov.krot.backend.application.context.loader.SessionContextLoader;
import com.egorgoncharov.krot.backend.application.events.bus.AbstractEventBus;
import com.egorgoncharov.krot.backend.application.events.bus.SessionEventBus;
import com.egorgoncharov.krot.backend.application.guard.modification.ModificationGuard;
import com.egorgoncharov.krot.backend.application.guard.modification.SessionModificationGuard;
import com.egorgoncharov.krot.backend.application.guard.view.SessionViewGuard;
import com.egorgoncharov.krot.backend.application.guard.view.ViewGuard;
import com.egorgoncharov.krot.backend.application.query.model.SessionQuery;
import com.egorgoncharov.krot.backend.application.service.SessionService;
import com.egorgoncharov.krot.backend.application.service.modification.ModificationService;
import com.egorgoncharov.krot.backend.database.relational.entity.HistoricalSessionEntity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SessionDomain extends AbstractCrudDomain<HistoricalSessionEntity, UUID, Void, SessionQuery> {
    private final SessionContextLoader contextLoader;
    private final SessionModificationGuard modificationGuard;
    private final SessionViewGuard visibilityGuard;
    private final SessionService service;
    private final SessionEventBus eventBus;

    @Inject
    public SessionDomain(SessionContextLoader contextLoader, SessionModificationGuard modificationGuard, SessionViewGuard visibilityGuard, SessionService service, SessionEventBus eventBus) {
        this.contextLoader = contextLoader;
        this.modificationGuard = modificationGuard;
        this.visibilityGuard = visibilityGuard;
        this.service = service;
        this.eventBus = eventBus;
    }

    @Override
    protected CrudContextLoader<HistoricalSessionEntity, UUID, Void, SessionQuery> contextLoader() {
        return contextLoader;
    }

    @Override
    protected ModificationGuard<HistoricalSessionEntity, UUID, Void> modificationGuard() {
        return modificationGuard;
    }

    @Override
    protected ViewGuard<HistoricalSessionEntity, UUID, Void> visibilityGuard() {
        return visibilityGuard;
    }

    @Override
    protected ModificationService<HistoricalSessionEntity, UUID, Void> service() {
        return service;
    }

    @Override
    protected AbstractEventBus<HistoricalSessionEntity, UUID, Void> eventBus() {
        return eventBus;
    }

    @Override
    public Uni<Result<List<HistoricalSessionEntity>>> delete(List<HistoricalSessionEntity> entities) {
        return Uni.createFrom().nullItem();
    }

    @Override
    public Uni<Result<HistoricalSessionEntity>> update(HistoricalSessionEntity entity) {
        return Uni.createFrom().nullItem();
    }
}
