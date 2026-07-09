package com.egorgoncharov.krot.backend.api.rest.core.device;

import com.egorgoncharov.krot.backend.api.rest.core.device.request.*;
import com.egorgoncharov.krot.backend.api.rest.core.device.response.Device;
import com.egorgoncharov.krot.backend.api.rest.request.DeleteRequest;
import com.egorgoncharov.krot.backend.api.rest.request.EntityReflection;
import com.egorgoncharov.krot.backend.api.rest.request.TransferOwnershipRequest;
import com.egorgoncharov.krot.backend.api.rest.response.ApiResponse;
import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.application.core.device.DeviceDomain;
import com.egorgoncharov.krot.backend.application.core.device.DeviceQuery;
import com.egorgoncharov.krot.backend.application.query.pagination.Page;
import com.egorgoncharov.krot.backend.database.relational.entity.DeviceCollaboratorEntity;
import com.egorgoncharov.krot.backend.database.relational.entity.DeviceEntity;
import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
import com.egorgoncharov.krot.backend.util.Types;
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
    private final DeviceDomain deviceDomain;

    @Inject
    public DeviceResource(DeviceDomain deviceDomain) {
        this.deviceDomain = deviceDomain;
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_DEVICE_READ", "DEVICE_READ", "SELF_READ"})
    public Uni<RestResponse<ApiResponse<List<EntityReflection<DeviceEntity>>>>> filterDevice(@Valid GetDeviceRequest body) {
        if (body.getIds() != null && !Types.validateUUID(body.getIds())) return Uni.createFrom().item(Result.<List<EntityReflection<DeviceEntity>>>badRequest().toRestResponse());
        return deviceDomain.query(DeviceQuery.builder()
                .lastUpdateTime(body.getLastUpdatedTime())
                .creationTime(body.getCreationTime())
                .ownerId(Types.toUUID(body.getOwnerId()))
                .nameQuery(body.getNameQuery())
                .addressQuery(body.getAddressQuery())
                .ids(Types.toUUID(body.getIds()))
                .pagination(body.getPagination())
                .build()
        ).chain(result -> {
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
        return deviceDomain.create(DeviceEntity.builder().name(body.getName()).password(body.getPassword()).build()).map(result -> result.voidCast().toRestResponse());
    }

    @PATCH
    @Path("/manage")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_DEVICE_UPSERT", "DEVICE_UPSERT", "SELF_UPSERT"})
    public Uni<RestResponse<ApiResponse<Void>>> editDevice(@Valid EditDeviceRequest body) {
        return deviceDomain.update(DeviceEntity.builder().id(Types.toUUID(body.getId())).name(body.getName()).password(body.getPassword()).build()).map(result -> result.voidCast().toRestResponse());
    }

    @PATCH
    @Path("/manage/collaborator/upsert")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_DEVICE_UPSERT", "DEVICE_UPSERT", "SELF_UPSERT"})
    public Uni<RestResponse<ApiResponse<Void>>> upsertDeviceCollaborator(@Valid UpsertDeviceCollaboratorRequest body) {
        return deviceDomain.upsertCollaborator(DeviceEntity.builder()
                .id(Types.toUUID(body.getDeviceId()))
                .collaborators(List.of(DeviceCollaboratorEntity.builder()
                        .collaborator(body.getUserId() == null ? null : UserEntity.builder().id(Types.toUUID(body.getUserId())).build())
                        .programs(Types.toUUID(body.getAllowPrograms()))
                        .canReadAddress(body.getCanReadAddress())
                        .canReadPassword(body.getCanReadPassword())
                        .canReadLastUpdate(body.getCanReadLastUpdate())
                        .canUpdateName(body.getCanUpdateName())
                        .canUpdatePassword(body.getCanUpdatePassword())
                        .build()
                ))
                .build()
        ).map(result -> result.voidCast().toRestResponse());
    }

    @POST
    @Path("/manage/collaborator/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_DEVICE_UPSERT", "DEVICE_UPSERT", "SELF_UPSERT"})
    public Uni<RestResponse<ApiResponse<Void>>> deleteDeviceCollaborator(@Valid DeleteDeviceCollaboratorRequest body) {
        return deviceDomain.deleteCollaborator(DeviceEntity.builder()
                .id(Types.toUUID(body.getId()))
                .collaborators(List.of(DeviceCollaboratorEntity.builder().id(Types.toUUID(body.getId())).build()))
                .build()
        ).map(result -> result.voidCast().toRestResponse());
    }

    @POST
    @Path("/transfer")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_DEVICE_TRANSFER_OWNERSHIP", "DEVICE_TRANSFER_OWNERSHIP", "SELF_DEVICE_TRANSFER_OWNERSHIP"})
    public Uni<RestResponse<ApiResponse<Void>>> transferDeviceOwnership(@Valid TransferOwnershipRequest body) {
        return deviceDomain.transferOwnership(DeviceEntity.builder()
                .id(Types.toUUID(body.getId()))
                .owner(UserEntity.builder().id(Types.toUUID(body.getNewOwnerId())).build())
                .build()
        ).map(result -> result.voidCast().toRestResponse());
    }

    @POST
    @Path("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_DEVICE_DELETE", "DEVICE_DELETE", "SELF_UPSERT"})
    public Uni<RestResponse<ApiResponse<Void>>> deleteDevice(@Valid DeleteRequest body) {
        if (!Types.validateUUID(body.getIds())) return Uni.createFrom().item(Result.<Void>badRequest().toRestResponse());
        return deviceDomain.delete(body.getIds().stream().map(id -> DeviceEntity.builder().id(Types.toUUID(id)).build()).toList()).map(result -> result.voidCast().toRestResponse());
    }
}
