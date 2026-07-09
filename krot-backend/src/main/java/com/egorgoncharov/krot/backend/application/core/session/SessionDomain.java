package com.egorgoncharov.krot.backend.application.core.session;

import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.application.context.loader.CrudContextLoader;
import com.egorgoncharov.krot.backend.application.domain.AbstractCrudDomain;
import com.egorgoncharov.krot.backend.application.evaluator.CrudPermissionsEvaluator;
import com.egorgoncharov.krot.backend.application.service.ModifyingService;
import com.egorgoncharov.krot.backend.database.relational.entity.HistoricalSessionEntity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SessionDomain extends AbstractCrudDomain<HistoricalSessionEntity, UUID, Void, SessionQuery> {
    private final SessionContextLoader contextLoader;
    private final SessionPermissionsEvaluator permissionsEvaluator;
    private final SessionService service;

    @Inject
    public SessionDomain(SessionContextLoader contextLoader, SessionPermissionsEvaluator permissionsEvaluator, SessionService service) {
        this.contextLoader = contextLoader;
        this.permissionsEvaluator = permissionsEvaluator;
        this.service = service;
    }

    @Override
    protected CrudContextLoader<HistoricalSessionEntity, UUID, Void, SessionQuery> contextLoader() {
        return contextLoader;
    }

    @Override
    protected CrudPermissionsEvaluator<HistoricalSessionEntity, UUID, Void> permissionsEvaluator() {
        return permissionsEvaluator;
    }

    @Override
    protected ModifyingService<HistoricalSessionEntity, UUID, Void> service() {
        return service;
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
