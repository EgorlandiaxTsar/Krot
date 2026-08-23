package com.egorgoncharov.krot.backend.application.guard.view;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.application.context.model.ViewContext;
import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
import com.egorgoncharov.krot.backend.security.Authority;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UserViewGuard implements ViewGuard<UserEntity, UUID, Void> {
    @Override
    public Uni<Result<List<Boolean>>> canView(ViewContext<UserEntity, UUID, Void> context) {
        boolean xUserRead = context.principal().hasAuthority(Authority.X_USER_READ);
        boolean userRead = context.principal().hasAuthority(Authority.USER_READ);
        boolean selfRead = context.principal().hasAuthority(Authority.SELF_READ);
        if (xUserRead) return Result.ok(context.targets().stream().map(e -> true).toList()).toUni();
        return Result.ok(context.targets().stream().map(user -> {
            if (user.getRole().getGrade() < context.principal().getRole().getGrade() && userRead) return true;
            return user.getId().equals(context.principal().getId()) && selfRead;
        }).toList()).toUni();
    }
}
