package com.egorgoncharov.krot.backend.application.guard.modification;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.application.context.model.CreationContext;
import com.egorgoncharov.krot.backend.application.context.model.DeleteContext;
import com.egorgoncharov.krot.backend.application.context.model.UpdateContext;
import com.egorgoncharov.krot.backend.database.relational.entity.DeviceCollaboratorEntity;
import com.egorgoncharov.krot.backend.database.relational.entity.DeviceEntity;
import com.egorgoncharov.krot.backend.security.Authority;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class DeviceModificationGuard implements ModificationGuard<DeviceEntity, UUID, Void> {
    @Override
    public Uni<Result<Boolean>> canCreate(CreationContext<DeviceEntity, UUID, Void> context) {
        boolean xDeviceUpsert = context.principal().hasAuthority(Authority.X_DEVICE_UPSERT);
        boolean deviceUpsert = context.principal().hasAuthority(Authority.DEVICE_UPSERT);
        boolean selfUpsert = context.principal().hasAuthority(Authority.SELF_UPSERT);
        return Result.ok(xDeviceUpsert || deviceUpsert || selfUpsert).toUni();
    }

    @Override
    public Uni<Result<Boolean>> canUpdate(UpdateContext<DeviceEntity, UUID, Void> context) {
        return Result.ok(canUpdateAsUser(context) || canUpdateAsCollaborator(context)).toUni();
    }

    @Override
    public Uni<Result<Boolean>> canDelete(DeleteContext<DeviceEntity, UUID, Void> context) {
        boolean xDeviceDelete = context.principal().hasAuthority(Authority.X_DEVICE_DELETE);
        boolean deviceDelete = context.principal().hasAuthority(Authority.DEVICE_DELETE);
        boolean selfUpsert = context.principal().hasAuthority(Authority.SELF_UPSERT);
        if (!xDeviceDelete && !deviceDelete && !selfUpsert) return Result.ok(false).toUni();
        if (xDeviceDelete) return Result.ok(true).toUni();
        return Result.ok(context.getEntities().stream().allMatch(device -> {
            if (device.getOwner().getRole().getGrade() < context.principal().getRole().getGrade() && deviceDelete) return true;
            return device.getOwner().getId().equals(context.principal().getId()) && selfUpsert;
        })).toUni();
    }

    public Uni<Result<Boolean>> canManageCollaborators(UpdateContext<DeviceEntity, UUID, Void> context) {
        return Result.ok(canUpdateAsUser(context)).toUni();
    }

    public Uni<Result<Boolean>> canTransferOwnership(UpdateContext<DeviceEntity, UUID, Void> context) {
        boolean xDeviceTransferOwnership = context.principal().hasAuthority(Authority.X_DEVICE_TRANSFER_OWNERSHIP);
        boolean deviceTransferOwnership = context.principal().hasAuthority(Authority.DEVICE_TRANSFER_OWNERSHIP);
        boolean selfDeviceTransferOwnership = context.principal().hasAuthority(Authority.SELF_DEVICE_TRANSFER_OWNERSHIP);
        if (!xDeviceTransferOwnership && !deviceTransferOwnership && !selfDeviceTransferOwnership) return Result.ok(false).toUni();
        if (xDeviceTransferOwnership) return Result.ok(true).toUni();
        if (context.getOldEntity().getOwner().getId().equals(context.principal().getId()) && selfDeviceTransferOwnership) return Result.ok(true).toUni();
        return Result.ok(context.getOldEntity().getOwner().getRole().getGrade() < context.principal().getRole().getGrade() && deviceTransferOwnership).toUni();
    }

    protected boolean canUpdateAsUser(UpdateContext<DeviceEntity, UUID, Void> context) {
        boolean xDeviceUpsert = context.principal().hasAuthority(Authority.X_DEVICE_UPSERT);
        boolean deviceUpsert = context.principal().hasAuthority(Authority.DEVICE_UPSERT);
        boolean selfUpsert = context.principal().hasAuthority(Authority.SELF_UPSERT);
        if (!xDeviceUpsert && !deviceUpsert && !selfUpsert) return false;
        if (xDeviceUpsert) return true;
        if (context.getOldEntity().getOwner().getId().equals(context.principal().getId()) && selfUpsert) return true;
        return context.getOldEntity().getOwner().getRole().getGrade() < context.principal().getRole().getGrade() && deviceUpsert;
    }

    protected boolean canUpdateAsCollaborator(UpdateContext<DeviceEntity, UUID, Void> context) {
        DeviceCollaboratorEntity collaborator = context
                .getOldEntity()
                .getCollaborators()
                .stream()
                .filter(c -> c.getCollaborator().getId().equals(context.principal().getId()))
                .findFirst()
                .orElse(null);
        if (collaborator == null) return false;
        if (!context.getOldEntity().getName().equals(context.getNewEntity().getName()) && context.getNewEntity().getName() != null && !collaborator.getCanUpdateName()) return false;
        return context.getOldEntity().getPassword().equals(context.getNewEntity().getPassword()) || context.getNewEntity().getPassword() == null || collaborator.getCanUpdatePassword();
    }
}
