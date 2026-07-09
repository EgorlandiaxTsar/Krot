package com.egorgoncharov.krot.backend.application.core.device;

import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.application.context.common.EntityCreationContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityDeleteContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityQueryContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityUpdateContext;
import com.egorgoncharov.krot.backend.application.evaluator.CrudPermissionsEvaluator;
import com.egorgoncharov.krot.backend.database.relational.entity.DeviceCollaboratorEntity;
import com.egorgoncharov.krot.backend.database.relational.entity.DeviceEntity;
import com.egorgoncharov.krot.backend.security.Authority;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DevicePermissionsEvaluator implements CrudPermissionsEvaluator<DeviceEntity, UUID, Void> {
    @Override
    public Uni<Result<Boolean>> canCreate(EntityCreationContext<DeviceEntity, UUID, Void> context) {
        boolean xDeviceUpsert = context.principal().hasAuthority(Authority.X_DEVICE_UPSERT);
        boolean deviceUpsert = context.principal().hasAuthority(Authority.DEVICE_UPSERT);
        boolean selfUpsert = context.principal().hasAuthority(Authority.SELF_UPSERT);
        return Result.ok(xDeviceUpsert || deviceUpsert || selfUpsert).toUni();
    }

    @Override
    public Uni<Result<List<Boolean>>> canView(EntityQueryContext<DeviceEntity, UUID, Void> context) {
        boolean xDeviceRead = context.principal().hasAuthority(Authority.X_DEVICE_READ);
        boolean deviceRead = context.principal().hasAuthority(Authority.DEVICE_READ);
        boolean selfRead = context.principal().hasAuthority(Authority.SELF_READ);
        if (xDeviceRead) return Result.ok(context.getResults().getItems().stream().map(e -> true).toList()).toUni();
        return Result.ok(context.getResults().getItems().stream().map(device -> {
            if (device.getCollaborators().stream().anyMatch(collaborator -> collaborator.getCollaborator().getId().equals(context.principal().getId()))) return true;
            if (device.getOwner().getRole().getGrade() < context.principal().getRole().getGrade() && deviceRead) return true;
            return device.getOwner().getId().equals(context.principal().getId()) && selfRead;
        }).toList()).toUni();
    }

    @Override
    public Uni<Result<Boolean>> canUpdate(EntityUpdateContext<DeviceEntity, UUID, Void> context) {
        return Result.ok(canUpdateAsUser(context) || canUpdateAsCollaborator(context)).toUni();
    }

    @Override
    public Uni<Result<Boolean>> canDelete(EntityDeleteContext<DeviceEntity, UUID, Void> context) {
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

    public Uni<Result<Boolean>> canManageCollaborators(EntityUpdateContext<DeviceEntity, UUID, Void> context) {
        return Result.ok(canUpdateAsUser(context)).toUni();
    }

    public Uni<Result<Boolean>> canTransferOwnership(EntityUpdateContext<DeviceEntity, UUID, Void> context) {
        boolean xDeviceTransferOwnership = context.principal().hasAuthority(Authority.X_DEVICE_TRANSFER_OWNERSHIP);
        boolean deviceTransferOwnership = context.principal().hasAuthority(Authority.DEVICE_TRANSFER_OWNERSHIP);
        boolean selfDeviceTransferOwnership = context.principal().hasAuthority(Authority.SELF_DEVICE_TRANSFER_OWNERSHIP);
        if (!xDeviceTransferOwnership && !deviceTransferOwnership && !selfDeviceTransferOwnership) return Result.ok(false).toUni();
        if (xDeviceTransferOwnership) return Result.ok(true).toUni();
        if (context.getOldEntity().getOwner().getId().equals(context.principal().getId()) && selfDeviceTransferOwnership) return Result.ok(true).toUni();
        return Result.ok(context.getOldEntity().getOwner().getRole().getGrade() < context.principal().getRole().getGrade() && deviceTransferOwnership).toUni();
    }

    protected boolean canUpdateAsUser(EntityUpdateContext<DeviceEntity, UUID, Void> context) {
        boolean xDeviceUpsert = context.principal().hasAuthority(Authority.X_DEVICE_UPSERT);
        boolean deviceUpsert = context.principal().hasAuthority(Authority.DEVICE_UPSERT);
        boolean selfUpsert = context.principal().hasAuthority(Authority.SELF_UPSERT);
        if (!xDeviceUpsert && !deviceUpsert && !selfUpsert) return false;
        if (xDeviceUpsert) return true;
        if (context.getOldEntity().getOwner().getId().equals(context.principal().getId()) && selfUpsert) return true;
        return context.getOldEntity().getOwner().getRole().getGrade() < context.principal().getRole().getGrade() && deviceUpsert;
    }

    protected boolean canUpdateAsCollaborator(EntityUpdateContext<DeviceEntity, UUID, Void> context) {
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
