package com.egorgoncharov.krot.backend.application.core.program;

import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.application.context.common.EntityCreationContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityDeleteContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityQueryContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityUpdateContext;
import com.egorgoncharov.krot.backend.application.evaluator.CrudPermissionsEvaluator;
import com.egorgoncharov.krot.backend.database.relational.entity.ProgramCollaboratorEntity;
import com.egorgoncharov.krot.backend.database.relational.entity.ProgramEntity;
import com.egorgoncharov.krot.backend.security.Authority;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ProgramPermissionsEvaluator implements CrudPermissionsEvaluator<ProgramEntity, UUID, Void> {
    @Override
    public Uni<Result<Boolean>> canCreate(EntityCreationContext<ProgramEntity, UUID, Void> context) {
        boolean xProgramUpsert = context.principal().hasAuthority(Authority.X_PROGRAM_UPSERT);
        boolean programUpsert = context.principal().hasAuthority(Authority.PROGRAM_UPSERT);
        boolean selfUpsert = context.principal().hasAuthority(Authority.SELF_UPSERT);
        return Result.ok(xProgramUpsert || programUpsert || selfUpsert).toUni();
    }

    @Override
    public Uni<Result<List<Boolean>>> canView(EntityQueryContext<ProgramEntity, UUID, Void> context) {
        boolean xProgramRead = context.principal().hasAuthority(Authority.X_PROGRAM_READ);
        boolean programRead = context.principal().hasAuthority(Authority.PROGRAM_READ);
        boolean selfRead = context.principal().hasAuthority(Authority.SELF_READ);
        if (xProgramRead) return Result.ok(context.getResults().getItems().stream().map(e -> true).toList()).toUni();
        return Result.ok(context.getResults().getItems().stream().map(program -> {
            if (program.getCollaborators().stream().anyMatch(collaborator -> collaborator.getCollaborator().getId().equals(context.principal().getId()))) return true;
            if (program.getOwner().getRole().getGrade() < context.principal().getRole().getGrade() && programRead) return true;
            return program.getOwner().getId().equals(context.principal().getId()) && selfRead;
        }).toList()).toUni();
    }

    @Override
    public Uni<Result<Boolean>> canUpdate(EntityUpdateContext<ProgramEntity, UUID, Void> context) {
        return Result.ok(canUpdateAsUser(context) || canUpdateAsCollaborator(context)).toUni();
    }

    @Override
    public Uni<Result<Boolean>> canDelete(EntityDeleteContext<ProgramEntity, UUID, Void> context) {
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

    public Uni<Result<Boolean>> canManageCollaborators(EntityUpdateContext<ProgramEntity, UUID, Void> context) {
        return Result.ok(canUpdateAsUser(context)).toUni();
    }

    public Uni<Result<Boolean>> canTransferOwnership(EntityUpdateContext<ProgramEntity, UUID, Void> context) {
        boolean xProgramTransferOwnership = context.principal().hasAuthority(Authority.X_PROGRAM_TRANSFER_OWNERSHIP);
        boolean programTransferOwnership = context.principal().hasAuthority(Authority.PROGRAM_TRANSFER_OWNERSHIP);
        boolean selfProgramTransferOwnership = context.principal().hasAuthority(Authority.SELF_PROGRAM_TRANSFER_OWNERSHIP);
        if (!xProgramTransferOwnership && !programTransferOwnership && !selfProgramTransferOwnership) return Result.ok(false).toUni();
        if (xProgramTransferOwnership) return Result.ok(true).toUni();
        if (context.getOldEntity().getOwner().getId().equals(context.principal().getId()) && selfProgramTransferOwnership) return Result.ok(true).toUni();
        return Result.ok(context.getOldEntity().getOwner().getRole().getGrade() < context.principal().getRole().getGrade() && programTransferOwnership).toUni();
    }

    protected boolean canUpdateAsUser(EntityUpdateContext<ProgramEntity, UUID, Void> context) {
        boolean xProgramUpsert = context.principal().hasAuthority(Authority.X_PROGRAM_UPSERT);
        boolean programUpsert = context.principal().hasAuthority(Authority.PROGRAM_UPSERT);
        boolean selfUpsert = context.principal().hasAuthority(Authority.SELF_UPSERT);
        if (!xProgramUpsert && !programUpsert && !selfUpsert) return false;
        if (xProgramUpsert) return true;
        if (context.getOldEntity().getOwner().getId().equals(context.principal().getId()) && selfUpsert) return true;
        return context.getOldEntity().getOwner().getRole().getGrade() < context.principal().getRole().getGrade() && programUpsert;
    }

    protected boolean canUpdateAsCollaborator(EntityUpdateContext<ProgramEntity, UUID, Void> context) {
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
