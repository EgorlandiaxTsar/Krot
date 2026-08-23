package com.egorgoncharov.krot.backend.application.guard.modification;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.application.context.model.CreationContext;
import com.egorgoncharov.krot.backend.application.context.model.DeleteContext;
import com.egorgoncharov.krot.backend.application.context.model.UpdateContext;
import com.egorgoncharov.krot.backend.database.relational.entity.RoleEntity;
import com.egorgoncharov.krot.backend.security.Authority;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class RoleModificationGuard implements ModificationGuard<RoleEntity, UUID, Void> {
    @Override
    public Uni<Result<Boolean>> canCreate(CreationContext<RoleEntity, UUID, Void> context) {
        boolean xRoleUpsert = context.principal().hasAuthority(Authority.X_ROLE_UPSERT);
        boolean xAuthorityManage = context.principal().hasAuthority(Authority.X_AUTHORITY_MANAGE);
        boolean roleUpsert = context.principal().hasAuthority(Authority.ROLE_UPSERT);
        if (context.getEntity().getAuthorities().stream().map(Enum::name).anyMatch(authority -> authority.startsWith("X")) && !xAuthorityManage) return Result.ok(false).toUni();
        if (xRoleUpsert) return Result.ok(true).toUni();
        if (!roleUpsert) return Result.ok(false).toUni();
        return Result.ok(context.principal().getRole().getGrade() < context.getEntity().getGrade()).toUni();
    }

    @Override
    public Uni<Result<Boolean>> canUpdate(UpdateContext<RoleEntity, UUID, Void> context) {
        boolean xRoleUpsert = context.principal().hasAuthority(Authority.X_ROLE_UPSERT);
        boolean xAuthorityManage = context.principal().hasAuthority(Authority.X_AUTHORITY_MANAGE);
        boolean roleUpsert = context.principal().hasAuthority(Authority.ROLE_UPSERT);
        if (context.getNewEntity().getAuthorities() != null) {
            Set<Authority> oldX = context.getOldEntity().getAuthorities().stream().filter(a -> a.name().startsWith("X")).collect(Collectors.toSet());
            Set<Authority> newX = context.getNewEntity().getAuthorities().stream().filter(a -> a.name().startsWith("X")).collect(Collectors.toSet());
            if (!oldX.equals(newX) && !xAuthorityManage) return Result.ok(false).toUni();
        }
        if (xRoleUpsert) return Result.ok(true).toUni();
        if (!roleUpsert) return Result.ok(false).toUni();
        return Result.ok(context.principal().getRole().getGrade() < context.getNewEntity().getGrade()).toUni();
    }

    @Override
    public Uni<Result<Boolean>> canDelete(DeleteContext<RoleEntity, UUID, Void> context) {
        boolean xRoleDelete = context.principal().hasAuthority(Authority.X_ROLE_DELETE);
        boolean roleDelete = context.principal().hasAuthority(Authority.ROLE_DELETE);
        if (xRoleDelete) return Result.ok(true).toUni();
        if (!roleDelete) return Result.ok(false).toUni();
        return Result.ok(context.getEntities().stream().allMatch(role -> role.getGrade() < context.principal().getRole().getGrade())).toUni();
    }
}
