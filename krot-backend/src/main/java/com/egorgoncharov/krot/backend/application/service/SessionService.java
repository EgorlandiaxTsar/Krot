package com.egorgoncharov.krot.backend.application.service;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.application.context.model.DeleteContext;
import com.egorgoncharov.krot.backend.application.context.model.UpdateContext;
import com.egorgoncharov.krot.backend.application.service.modification.AbstractModificationService;
import com.egorgoncharov.krot.backend.database.relational.RelationalCrudRepository;
import com.egorgoncharov.krot.backend.database.relational.entity.HistoricalSessionEntity;
import com.egorgoncharov.krot.backend.database.relational.repository.HistoricalSessionRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SessionService extends AbstractModificationService<HistoricalSessionEntity, UUID, Void> {
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
    public Uni<Result<HistoricalSessionEntity>> update(UpdateContext<HistoricalSessionEntity, UUID, Void> context) {
        return Uni.createFrom().nullItem();
    }

    @Override
    public Uni<Result<List<HistoricalSessionEntity>>> delete(DeleteContext<HistoricalSessionEntity, UUID, Void> context) {
        return Uni.createFrom().nullItem();
    }
}
