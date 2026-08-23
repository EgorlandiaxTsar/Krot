package com.egorgoncharov.krot.backend.application.guard.view;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.application.context.model.ViewContext;
import com.egorgoncharov.krot.backend.database.relational.entity.DeviceEntity;
import com.egorgoncharov.krot.backend.security.Authority;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DeviceViewGuard implements ViewGuard<DeviceEntity, UUID, Void> {
    @Override
    public Uni<Result<List<Boolean>>> canView(ViewContext<DeviceEntity, UUID, Void> context) {
        boolean xDeviceRead = context.principal().hasAuthority(Authority.X_DEVICE_READ);
        boolean deviceRead = context.principal().hasAuthority(Authority.DEVICE_READ);
        boolean selfRead = context.principal().hasAuthority(Authority.SELF_READ);
        if (xDeviceRead) return Result.ok(context.targets().stream().map(e -> true).toList()).toUni();
        return Result.ok(context.targets().stream().map(device -> {
            if (device.getCollaborators().stream().anyMatch(collaborator -> collaborator.getCollaborator().getId().equals(context.principal().getId()))) return true;
            if (device.getOwner().getRole().getGrade() < context.principal().getRole().getGrade() && deviceRead) return true;
            return device.getOwner().getId().equals(context.principal().getId()) && selfRead;
        }).toList()).toUni();
    }
}
