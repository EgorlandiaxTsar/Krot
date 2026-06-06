package com.egorgoncharov.krot.backend.resource;

import com.egorgoncharov.krot.backend.dto.api.ApiResponse;
import com.egorgoncharov.krot.backend.dto.api.request.DeleteRequest;
import com.egorgoncharov.krot.backend.dto.api.request.EntityReflection;
import com.egorgoncharov.krot.backend.dto.api.request.TransferOwnershipRequest;
import com.egorgoncharov.krot.backend.dto.api.request.device.*;
import com.egorgoncharov.krot.backend.dto.api.response.Device;
import com.egorgoncharov.krot.backend.dto.service.Result;
import com.egorgoncharov.krot.backend.dto.service.pagination.Page;
import com.egorgoncharov.krot.backend.model.entity.DeviceCollaboratorEntity;
import com.egorgoncharov.krot.backend.model.entity.DeviceEntity;
import com.egorgoncharov.krot.backend.model.entity.UserEntity;
import com.egorgoncharov.krot.backend.service.helper.TypesHelper;
import com.egorgoncharov.krot.backend.service.impl.DeviceServiceImpl;
import io.quarkus.security.PermissionsAllowed;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.List;

@Path("/api/device")
public class DeviceResource {
    private final DeviceServiceImpl deviceService;

    @Inject
    public DeviceResource(DeviceServiceImpl deviceService) {
        this.deviceService = deviceService;
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_DEVICE_READ", "DEVICE_READ", "SELF_READ"})
    public Uni<RestResponse<ApiResponse<List<EntityReflection<DeviceEntity>>>>> filterDevice(@Valid GetDeviceRequest body) {
        if (body.getIds() != null && !TypesHelper.validateUUID(body.getIds())) return Uni.createFrom().item(Result.<List<EntityReflection<DeviceEntity>>>badRequest().toRestResponse());
        return deviceService.filter(TypesHelper.toUUID(body.getIds()), body.getLastUpdatedTime(), body.getCreationTime(), TypesHelper.toUUID(body.getOwnerId()), body.getNameQuery(), body.getAddressQuery(), body.getPagination()).chain(result -> {
            if (result.getCode() != 200 || result.getResult().isEmpty()) return Uni.createFrom().item(result.<List<EntityReflection<DeviceEntity>>>nullCast().toRestResponse());
            Page<DeviceEntity> data = result.getResult().get();
            return Uni.createFrom().item(new ApiResponse<>(result.toApiMetadata(), data.getItems().stream().map(e -> new Device().from(e)).toList(), data).toRestResponse());
        });
    }

    @POST
    @Path("/manage")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_DEVICE_UPSERT", "DEVICE_UPSERT", "SELF_UPSERT"})
    public Uni<RestResponse<ApiResponse<Void>>> createDevice(@Valid CreateDeviceRequest body) {
        return deviceService.create(DeviceEntity.builder().name(body.getName()).password(body.getPassword()).build()).map(result -> result.voidCast().toRestResponse());
    }

    @PATCH
    @Path("/manage")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_DEVICE_UPSERT", "DEVICE_UPSERT", "SELF_UPSERT"})
    public Uni<RestResponse<ApiResponse<Void>>> editDevice(@Valid EditDeviceRequest body) {
        return deviceService.update(DeviceEntity.builder().id(TypesHelper.toUUID(body.getId())).name(body.getName()).password(body.getPassword()).build()).map(result -> result.voidCast().toRestResponse());
    }

    @PATCH
    @Path("/manage/collaborator/upsert")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_DEVICE_UPSERT", "DEVICE_UPSERT", "SELF_UPSERT"})
    public Uni<RestResponse<ApiResponse<Void>>> upsertDeviceCollaborator(@Valid UpsertDeviceCollaboratorRequest body) {
        return deviceService.upsertCollaborator(DeviceCollaboratorEntity.builder().collaborator(body.getUserId() == null ? null : UserEntity.builder().id(TypesHelper.toUUID(body.getUserId())).build()).device(DeviceEntity.builder().id(TypesHelper.toUUID(body.getDeviceId())).build()).programs(TypesHelper.toUUID(body.getAllowPrograms())).canReadAddress(body.getCanReadAddress()).canReadPassword(body.getCanReadPassword()).canReadLastUpdate(body.getCanReadLastUpdate()).canUpdateName(body.getCanUpdateName()).canUpdatePassword(body.getCanUpdatePassword()).build()).map(result -> result.voidCast().toRestResponse());
    }

    @POST
    @Path("/manage/collaborator/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_DEVICE_UPSERT", "DEVICE_UPSERT", "SELF_UPSERT"})
    public Uni<RestResponse<ApiResponse<Void>>> deleteDeviceCollaborator(@Valid DeleteDeviceCollaboratorRequest body) {
        return deviceService.deleteCollaborator(DeviceCollaboratorEntity.builder().id(TypesHelper.toUUID(body.getId())).build()).map(result -> result.voidCast().toRestResponse());
    }

    @POST
    @Path("/transfer")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_DEVICE_TRANSFER_OWNERSHIP", "DEVICE_TRANSFER_OWNERSHIP", "SELF_DEVICE_TRANSFER_OWNERSHIP"})
    public Uni<RestResponse<ApiResponse<Void>>> transferDeviceOwnership(@Valid TransferOwnershipRequest body) {
        return deviceService.transferOwnership(TypesHelper.toUUID(body.getId()), TypesHelper.toUUID(body.getNewOwnerId())).map(result -> result.voidCast().toRestResponse());
    }

    @POST
    @Path("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_DEVICE_DELETE", "DEVICE_DELETE", "SELF_UPSERT"})
    public Uni<RestResponse<ApiResponse<Void>>> deleteDevice(@Valid DeleteRequest body) {
        if (!TypesHelper.validateUUID(body.getIds())) return Uni.createFrom().item(Result.<Void>badRequest().toRestResponse());
        return deviceService.deleteById(TypesHelper.toUUID(body.getIds())).map(result -> result.voidCast().toRestResponse());
    }
}
