package com.egorgoncharov.krot.backend.application.guard.modification;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.application.context.model.CreationContext;
import com.egorgoncharov.krot.backend.application.context.model.DeleteContext;
import com.egorgoncharov.krot.backend.application.context.model.UpdateContext;
import com.egorgoncharov.krot.backend.database.relational.entity.HistoricalSessionEntity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class SessionModificationGuard implements ModificationGuard<HistoricalSessionEntity, UUID, Void> {
    @Override
    public Uni<Result<Boolean>> canCreate(CreationContext<HistoricalSessionEntity, UUID, Void> context) {
        return Result.ok(true).toUni();
    }

    @Override
    public Uni<Result<Boolean>> canUpdate(UpdateContext<HistoricalSessionEntity, UUID, Void> context) {
        return Result.ok(false).toUni();
    }

    @Override
    public Uni<Result<Boolean>> canDelete(DeleteContext<HistoricalSessionEntity, UUID, Void> context) {
        return Result.ok(false).toUni();
    }
}
