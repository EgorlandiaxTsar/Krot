package com.egorgoncharov.krot.backend.application.guard.view;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.application.context.model.ViewContext;
import com.egorgoncharov.krot.backend.database.relational.entity.ProgramEntity;
import com.egorgoncharov.krot.backend.security.Authority;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ProgramViewGuard implements ViewGuard<ProgramEntity, UUID, Void> {
    @Override
    public Uni<Result<List<Boolean>>> canView(ViewContext<ProgramEntity, UUID, Void> context) {
        boolean xProgramRead = context.principal().hasAuthority(Authority.X_PROGRAM_READ);
        boolean programRead = context.principal().hasAuthority(Authority.PROGRAM_READ);
        boolean selfRead = context.principal().hasAuthority(Authority.SELF_READ);
        if (xProgramRead) return Result.ok(context.targets().stream().map(e -> true).toList()).toUni();
        return Result.ok(context.targets().stream().map(program -> {
            if (program.getCollaborators().stream().anyMatch(collaborator -> collaborator.getCollaborator().getId().equals(context.principal().getId()))) return true;
            if (program.getOwner().getRole().getGrade() < context.principal().getRole().getGrade() && programRead) return true;
            return program.getOwner().getId().equals(context.principal().getId()) && selfRead;
        }).toList()).toUni();
    }
}
