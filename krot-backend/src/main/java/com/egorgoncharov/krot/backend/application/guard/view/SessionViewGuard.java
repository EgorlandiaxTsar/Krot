package com.egorgoncharov.krot.backend.application.guard.view;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.application.context.model.ViewContext;
import com.egorgoncharov.krot.backend.database.relational.entity.HistoricalSessionEntity;
import com.egorgoncharov.krot.backend.security.Authority;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SessionViewGuard implements ViewGuard<HistoricalSessionEntity, UUID, Void> {
    @Override
    public Uni<Result<List<Boolean>>> canView(ViewContext<HistoricalSessionEntity, UUID, Void> context) {
        boolean xSessionRead = context.principal().hasAuthority(Authority.X_SESSION_READ);
        boolean sessionRead = context.principal().hasAuthority(Authority.SESSION_READ);
        boolean selfRead = context.principal().hasAuthority(Authority.SELF_READ);
        if (xSessionRead) return Result.ok(context.targets().stream().map(e -> true).toList()).toUni();
        return Result.ok(context.targets().stream().map(session -> {
            if ((session.getUserOwner() != null && session.getUserOwner().getRole().getGrade() < context.principal().getRole().getGrade()) || (session.getDeviceOwner() != null && session.getDeviceOwner().getOwner().getRole().getGrade() < context.principal().getRole().getGrade()) && sessionRead) return true;
            return ((session.getUserOwner() != null && session.getUserOwner().getId().equals(context.principal().getId())) || (session.getDeviceOwner() != null && session.getDeviceOwner().getOwner().getId().equals(context.principal().getId())) && selfRead);
        }).toList()).toUni();
    }
}
