package com.egorgoncharov.krot.backend.application.core.user;

import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.application.context.common.EntityCreationContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityDeleteContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityQueryContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityUpdateContext;
import com.egorgoncharov.krot.backend.application.evaluator.CrudPermissionsEvaluator;
import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
import com.egorgoncharov.krot.backend.security.Authority;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UserPermissionsEvaluator implements CrudPermissionsEvaluator<UserEntity, UUID, Void> {
    @Override
    public Uni<Result<Boolean>> canCreate(EntityCreationContext<UserEntity, UUID, Void> context) {
        boolean xUserUpsert = context.principal().hasAuthority(Authority.X_USER_UPSERT);
        boolean xRoleAssign = context.principal().hasAuthority(Authority.X_ROLE_ASSIGN);
        boolean userUpsert = context.principal().hasAuthority(Authority.USER_UPSERT);
        if (!xUserUpsert && !userUpsert) return Result.ok(false).toUni();
        if (context.getEntity().getRole().getGrade() >= context.principal().getRole().getGrade() && !xRoleAssign) return Result.ok(false).toUni();
        return Result.ok(true).toUni();
    }

    @Override
    public Uni<Result<List<Boolean>>> canView(EntityQueryContext<UserEntity, UUID, Void> context) {
        boolean xUserRead = context.principal().hasAuthority(Authority.X_USER_READ);
        boolean userRead = context.principal().hasAuthority(Authority.USER_READ);
        boolean selfRead = context.principal().hasAuthority(Authority.SELF_READ);
        if (xUserRead) return Result.ok(context.getResults().getItems().stream().map(e -> true).toList()).toUni();
        return Result.ok(context.getResults().getItems().stream().map(user -> {
            if (user.getRole().getGrade() < context.principal().getRole().getGrade() && userRead) return true;
            return user.getId().equals(context.principal().getId()) && selfRead;
        }).toList()).toUni();
    }

    @Override
    public Uni<Result<Boolean>> canUpdate(EntityUpdateContext<UserEntity, UUID, Void> context) {
        boolean xUserUpsert = context.principal().hasAuthority(Authority.X_USER_UPSERT);
        boolean xRoleAssign = context.principal().hasAuthority(Authority.X_ROLE_ASSIGN);
        boolean userUpsert = context.principal().hasAuthority(Authority.USER_UPSERT);
        boolean selfUpsert = context.principal().hasAuthority(Authority.SELF_UPSERT);
        if (!xUserUpsert && !userUpsert && !selfUpsert) return Result.ok(false).toUni();
        if (context.getNewEntity().getRole() != null && context.getNewEntity().getRole().getGrade() >= context.principal().getRole().getGrade() && !xRoleAssign) return Result.ok(false).toUni();
        if (xUserUpsert) return Result.ok(true).toUni();
        if (context.getOldEntity().getId().equals(context.principal().getId()) && selfUpsert) return Result.ok(true).toUni();
        return Result.ok(context.getOldEntity().getRole().getGrade() < context.principal().getRole().getGrade() && userUpsert).toUni();
    }

    @Override
    public Uni<Result<Boolean>> canDelete(EntityDeleteContext<UserEntity, UUID, Void> context) {
        boolean xUserDelete = context.principal().hasAuthority(Authority.X_USER_DELETE);
        boolean userDelete = context.principal().hasAuthority(Authority.USER_DELETE);
        boolean selfDelete = context.principal().hasAuthority(Authority.SELF_DELETE);
        if (!xUserDelete && !userDelete && !selfDelete) return Result.ok(false).toUni();
        if (xUserDelete) return Result.ok(true).toUni();
        return Result.ok(context.getEntities().stream().allMatch(user -> {
            if (user.getRole().getGrade() < context.principal().getRole().getGrade() && userDelete) return true;
            return user.getId().equals(context.principal().getId()) && selfDelete;
        })).toUni();
    }


    public Uni<Result<Boolean>> canUpdatePassword(EntityUpdateContext<UserEntity, UUID, Void> context) {
        if (!context.principal().getId().equals(context.getOldEntity().getId())) return Result.ok(false).toUni();
        if (!context.principal().getPassword().equals(context.getOldEntity().getPassword())) return Result.ok(false).toUni();
        boolean xUpsertPassword = context.principal().hasAuthority(Authority.SELF_UPSERT_PASSWORD);
        if (!xUpsertPassword) return Result.ok(false).toUni();
        return Result.ok(true).toUni();
    }
}
