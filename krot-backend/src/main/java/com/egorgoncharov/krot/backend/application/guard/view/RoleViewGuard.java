package com.egorgoncharov.krot.backend.application.guard.view;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.application.context.model.ViewContext;
import com.egorgoncharov.krot.backend.database.relational.entity.RoleEntity;
import com.egorgoncharov.krot.backend.security.Authority;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class RoleViewGuard implements ViewGuard<RoleEntity, UUID, Void> {
    @Override
    public Uni<Result<List<Boolean>>> canView(ViewContext<RoleEntity, UUID, Void> context) {
        boolean xRoleRead = context.principal().hasAuthority(Authority.X_ROLE_READ);
        boolean roleRead = context.principal().hasAuthority(Authority.ROLE_READ);
        boolean selfRead = context.principal().hasAuthority(Authority.SELF_READ);
        if (xRoleRead) return Result.ok(context.targets().stream().map(e -> true).toList()).toUni();
        return Result.ok(context.targets().stream().map(role -> {
            if (role.getGrade() < context.principal().getRole().getGrade() && roleRead) return true;
            return role.getId().equals(context.principal().getRole().getId()) && selfRead;
        }).toList()).toUni();
    }
}
