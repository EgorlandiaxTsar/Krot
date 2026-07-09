package com.egorgoncharov.krot.backend.application.core.session;

import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.application.context.common.EntityCreationContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityDeleteContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityQueryContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityUpdateContext;
import com.egorgoncharov.krot.backend.application.evaluator.CrudPermissionsEvaluator;
import com.egorgoncharov.krot.backend.database.relational.entity.HistoricalSessionEntity;
import com.egorgoncharov.krot.backend.security.Authority;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SessionPermissionsEvaluator implements CrudPermissionsEvaluator<HistoricalSessionEntity, UUID, Void> {
    @Override
    public Uni<Result<Boolean>> canCreate(EntityCreationContext<HistoricalSessionEntity, UUID, Void> context) {
        return Result.ok(true).toUni();
    }

    @Override
    public Uni<Result<List<Boolean>>> canView(EntityQueryContext<HistoricalSessionEntity, UUID, Void> context) {
        boolean xSessionRead = context.principal().hasAuthority(Authority.X_SESSION_READ);
        boolean sessionRead = context.principal().hasAuthority(Authority.SESSION_READ);
        boolean selfRead = context.principal().hasAuthority(Authority.SELF_READ);
        if (xSessionRead) return Result.ok(context.getResults().getItems().stream().map(e -> true).toList()).toUni();
        return Result.ok(context.getResults().getItems().stream().map(session -> {
            if ((session.getUserOwner() != null && session.getUserOwner().getRole().getGrade() < context.principal().getRole().getGrade()) || (session.getDeviceOwner() != null && session.getDeviceOwner().getOwner().getRole().getGrade() < context.principal().getRole().getGrade()) && sessionRead) return true;
            return ((session.getUserOwner() != null && session.getUserOwner().getId().equals(context.principal().getId())) || (session.getDeviceOwner() != null && session.getDeviceOwner().getOwner().getId().equals(context.principal().getId())) && selfRead);
        }).toList()).toUni();
    }

    @Override
    public Uni<Result<Boolean>> canUpdate(EntityUpdateContext<HistoricalSessionEntity, UUID, Void> context) {
        return Result.ok(false).toUni();
    }

    @Override
    public Uni<Result<Boolean>> canDelete(EntityDeleteContext<HistoricalSessionEntity, UUID, Void> context) {
        return Result.ok(false).toUni();
    }
}
