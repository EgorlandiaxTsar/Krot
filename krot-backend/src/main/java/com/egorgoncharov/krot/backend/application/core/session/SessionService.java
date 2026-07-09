package com.egorgoncharov.krot.backend.application.core.session;

import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.application.context.common.EntityDeleteContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityUpdateContext;
import com.egorgoncharov.krot.backend.application.service.AbstractModifyingService;
import com.egorgoncharov.krot.backend.database.relational.RelationalCrudRepository;
import com.egorgoncharov.krot.backend.database.relational.entity.HistoricalSessionEntity;
import com.egorgoncharov.krot.backend.database.relational.repository.HistoricalSessionRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SessionService extends AbstractModifyingService<HistoricalSessionEntity, UUID, Void> {
    private final HistoricalSessionRepository repository;

    @Inject
    public SessionService(HistoricalSessionRepository repository) {
        this.repository = repository;
    }

    @Override
    protected RelationalCrudRepository<HistoricalSessionEntity, UUID> repository() {
        return repository;
    }

    @Override
    public Uni<Result<HistoricalSessionEntity>> update(EntityUpdateContext<HistoricalSessionEntity, UUID, Void> context) {
        return Uni.createFrom().nullItem();
    }

    @Override
    public Uni<Result<List<HistoricalSessionEntity>>> delete(EntityDeleteContext<HistoricalSessionEntity, UUID, Void> context) {
        return Uni.createFrom().nullItem();
    }
}
