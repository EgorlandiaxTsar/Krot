package com.egorgoncharov.krot.backend.application.guard.modification;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.application.context.model.CreationContext;
import com.egorgoncharov.krot.backend.application.context.model.DeleteContext;
import com.egorgoncharov.krot.backend.application.context.model.UpdateContext;
import com.egorgoncharov.krot.backend.database.relational.entity.ProgramCollaboratorEntity;
import com.egorgoncharov.krot.backend.database.relational.entity.ProgramEntity;
import com.egorgoncharov.krot.backend.security.Authority;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class ProgramModificationGuard implements ModificationGuard<ProgramEntity, UUID, Void> {
    @Override
    public Uni<Result<Boolean>> canCreate(CreationContext<ProgramEntity, UUID, Void> context) {
        boolean xProgramUpsert = context.principal().hasAuthority(Authority.X_PROGRAM_UPSERT);
        boolean programUpsert = context.principal().hasAuthority(Authority.PROGRAM_UPSERT);
        boolean selfUpsert = context.principal().hasAuthority(Authority.SELF_UPSERT);
        return Result.ok(xProgramUpsert || programUpsert || selfUpsert).toUni();
    }

    @Override
    public Uni<Result<Boolean>> canUpdate(UpdateContext<ProgramEntity, UUID, Void> context) {
        return Result.ok(canUpdateAsUser(context) || canUpdateAsCollaborator(context)).toUni();
    }

    @Override
    public Uni<Result<Boolean>> canDelete(DeleteContext<ProgramEntity, UUID, Void> context) {
        boolean xProgramDelete = context.principal().hasAuthority(Authority.X_PROGRAM_DELETE);
        boolean programDelete = context.principal().hasAuthority(Authority.PROGRAM_DELETE);
        boolean selfUpsert = context.principal().hasAuthority(Authority.SELF_UPSERT);
        if (!xProgramDelete && !programDelete && !selfUpsert) return Result.ok(false).toUni();
        if (xProgramDelete) return Result.ok(true).toUni();
        return Result.ok(context.getEntities().stream().allMatch(program -> {
            if (program.getOwner().getRole().getGrade() < context.principal().getRole().getGrade() && programDelete) return true;
            return program.getOwner().getId().equals(context.principal().getId()) && selfUpsert;
        })).toUni();
    }

    public Uni<Result<Boolean>> canManageCollaborators(UpdateContext<ProgramEntity, UUID, Void> context) {
        return Result.ok(canUpdateAsUser(context)).toUni();
    }

    public Uni<Result<Boolean>> canTransferOwnership(UpdateContext<ProgramEntity, UUID, Void> context) {
        boolean xProgramTransferOwnership = context.principal().hasAuthority(Authority.X_PROGRAM_TRANSFER_OWNERSHIP);
        boolean programTransferOwnership = context.principal().hasAuthority(Authority.PROGRAM_TRANSFER_OWNERSHIP);
        boolean selfProgramTransferOwnership = context.principal().hasAuthority(Authority.SELF_PROGRAM_TRANSFER_OWNERSHIP);
        if (!xProgramTransferOwnership && !programTransferOwnership && !selfProgramTransferOwnership) return Result.ok(false).toUni();
        if (xProgramTransferOwnership) return Result.ok(true).toUni();
        if (context.getOldEntity().getOwner().getId().equals(context.principal().getId()) && selfProgramTransferOwnership) return Result.ok(true).toUni();
        return Result.ok(context.getOldEntity().getOwner().getRole().getGrade() < context.principal().getRole().getGrade() && programTransferOwnership).toUni();
    }

    protected boolean canUpdateAsUser(UpdateContext<ProgramEntity, UUID, Void> context) {
        boolean xProgramUpsert = context.principal().hasAuthority(Authority.X_PROGRAM_UPSERT);
        boolean programUpsert = context.principal().hasAuthority(Authority.PROGRAM_UPSERT);
        boolean selfUpsert = context.principal().hasAuthority(Authority.SELF_UPSERT);
        if (!xProgramUpsert && !programUpsert && !selfUpsert) return false;
        if (xProgramUpsert) return true;
        if (context.getOldEntity().getOwner().getId().equals(context.principal().getId()) && selfUpsert) return true;
        return context.getOldEntity().getOwner().getRole().getGrade() < context.principal().getRole().getGrade() && programUpsert;
    }

    protected boolean canUpdateAsCollaborator(UpdateContext<ProgramEntity, UUID, Void> context) {
        ProgramCollaboratorEntity collaborator = context
                .getOldEntity()
                .getCollaborators()
                .stream()
                .filter(c -> c.getCollaborator().getId().equals(context.principal().getId()))
                .findFirst()
                .orElse(null);
        if (collaborator == null) return false;
        return context.getOldEntity().getName().equals(context.getNewEntity().getName()) || context.getNewEntity().getName() == null || collaborator.getCanUpdateName();
    }
}
