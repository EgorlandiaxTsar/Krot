package com.egorgoncharov.krot.backend.resource;

import com.egorgoncharov.krot.backend.dto.api.ApiResponse;
import com.egorgoncharov.krot.backend.dto.api.request.DeleteRequest;
import com.egorgoncharov.krot.backend.dto.api.request.EntityReflection;
import com.egorgoncharov.krot.backend.dto.api.request.user.CreateUserRequest;
import com.egorgoncharov.krot.backend.dto.api.request.user.EditUserPasswordRequest;
import com.egorgoncharov.krot.backend.dto.api.request.user.EditUserRequest;
import com.egorgoncharov.krot.backend.dto.api.request.user.GetUserRequest;
import com.egorgoncharov.krot.backend.dto.api.response.User;
import com.egorgoncharov.krot.backend.dto.service.Result;
import com.egorgoncharov.krot.backend.dto.service.pagination.Page;
import com.egorgoncharov.krot.backend.model.entity.RoleEntity;
import com.egorgoncharov.krot.backend.model.entity.UserEntity;
import com.egorgoncharov.krot.backend.service.helper.TypesHelper;
import com.egorgoncharov.krot.backend.service.impl.UserServiceImpl;
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

@Path("/api/user")
public class UserResource {
    private final UserServiceImpl userService;

    @Inject
    public UserResource(UserServiceImpl userService) {
        this.userService = userService;
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_USER_READ", "USER_READ", "SELF_READ"})
    public Uni<RestResponse<ApiResponse<List<EntityReflection<UserEntity>>>>> filterUser(@Valid GetUserRequest body) {
        if (body.getIds() != null && !TypesHelper.validateUUID(body.getIds())) return Uni.createFrom().item(Result.<List<EntityReflection<UserEntity>>>badRequest().toRestResponse());
        return userService.filter(TypesHelper.toUUID(body.getIds()), body.getActive(), body.getCreationTime(), TypesHelper.toUUID(body.getRoleId()), body.getUsernameQuery(), body.getPagination()).chain(result -> {
            if (result.getCode() != 200 || result.getResult().isEmpty()) return Uni.createFrom().item(result.<List<EntityReflection<UserEntity>>>nullCast().toRestResponse());
            Page<UserEntity> data = result.getResult().get();
            return Uni.createFrom().item(new ApiResponse<>(result.toApiMetadata(), data.getItems().stream().map(e -> new User().from(e)).toList(), data).toRestResponse());
        });
    }

    @POST
    @Path("/manage")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_USER_UPSERT", "USER_UPSERT"})
    public Uni<RestResponse<ApiResponse<Void>>> createUser(@Valid CreateUserRequest body) {
        return userService.create(UserEntity.builder().username(body.getUsername()).password(body.getPassword()).role(RoleEntity.builder().id(TypesHelper.toUUID(body.getRoleId())).build()).build()).map(result -> result.voidCast().toRestResponse());
    }

    @PATCH
    @Path("/manage")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_USER_UPSERT", "USER_UPSERT", "SELF_UPSERT"})
    public Uni<RestResponse<ApiResponse<Void>>> editUser(@Valid EditUserRequest body) {
        return userService.update(UserEntity.builder().id(TypesHelper.toUUID(body.getId())).username(body.getUsername()).role(body.getRoleId() == null ? null : RoleEntity.builder().id(TypesHelper.toUUID(body.getRoleId())).build()).active(body.getActive()).build()).map(result -> result.voidCast().toRestResponse());
    }

    @PATCH
    @Path("/manage/password")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"SELF_UPSERT_PASSWORD"})
    public Uni<RestResponse<ApiResponse<Void>>> editUserPassword(@Valid EditUserPasswordRequest body) {
        return userService.updatePassword(UserEntity.builder().id(TypesHelper.toUUID(body.getId())).password(body.getPassword()).build(), body.getNewPassword()).map(result -> result.voidCast().toRestResponse());
    }

    @POST
    @Path("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_USER_DELETE", "USER_DELETE", "SELF_DELETE"})
    public Uni<RestResponse<ApiResponse<Void>>> deleteUser(@Valid DeleteRequest body) {
        if (!TypesHelper.validateUUID(body.getIds())) return Uni.createFrom().item(Result.<Void>badRequest().toRestResponse());
        return userService.deleteById(TypesHelper.toUUID(body.getIds())).map(result -> result.voidCast().toRestResponse());
    }
}
