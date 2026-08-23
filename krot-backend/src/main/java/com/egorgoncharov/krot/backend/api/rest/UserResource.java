package com.egorgoncharov.krot.backend.api.rest;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.api.model.request.DeleteRequest;
import com.egorgoncharov.krot.backend.api.model.request.EntityReflection;
import com.egorgoncharov.krot.backend.api.model.request.user.CreateUserRequest;
import com.egorgoncharov.krot.backend.api.model.request.user.EditUserPasswordRequest;
import com.egorgoncharov.krot.backend.api.model.request.user.EditUserRequest;
import com.egorgoncharov.krot.backend.api.model.request.user.GetUserRequest;
import com.egorgoncharov.krot.backend.api.model.response.ApiResponse;
import com.egorgoncharov.krot.backend.api.model.response.user.User;
import com.egorgoncharov.krot.backend.application.domain.UserDomain;
import com.egorgoncharov.krot.backend.application.query.model.UserQuery;
import com.egorgoncharov.krot.backend.application.query.pagination.Page;
import com.egorgoncharov.krot.backend.database.relational.entity.RoleEntity;
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

@Path("/api/user")
public class UserResource {
    private final UserDomain userDomain;

    @Inject
    public UserResource(UserDomain userDomain) {
        this.userDomain = userDomain;
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_USER_READ", "USER_READ", "SELF_READ"})
    public Uni<RestResponse<ApiResponse<List<EntityReflection<UserEntity>>>>> filterUser(@Valid GetUserRequest body) {
        if (body.getIds() != null && !Types.validateUUID(body.getIds())) return Uni.createFrom().item(Result.<List<EntityReflection<UserEntity>>>badRequest().toRestResponse());
        return userDomain.query(UserQuery.builder()
                .active(body.getActive())
                .creationTime(body.getCreationTime())
                .roleId(Types.toUUID(body.getRoleId()))
                .usernameQuery(body.getUsernameQuery())
                .ids(Types.toUUID(body.getIds()))
                .pagination(body.getPagination())
                .build()
        ).chain(result -> {
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
        return userDomain.create(UserEntity.builder().username(body.getUsername()).password(body.getPassword()).role(RoleEntity.builder().id(Types.toUUID(body.getRoleId())).build()).build()).map(result -> result.voidCast().toRestResponse());
    }

    @PATCH
    @Path("/manage")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_USER_UPSERT", "USER_UPSERT", "SELF_UPSERT"})
    public Uni<RestResponse<ApiResponse<Void>>> editUser(@Valid EditUserRequest body) {
        return userDomain.update(UserEntity.builder().id(Types.toUUID(body.getId())).username(body.getUsername()).role(body.getRoleId() == null ? null : RoleEntity.builder().id(Types.toUUID(body.getRoleId())).build()).active(body.getActive()).build()).map(result -> result.voidCast().toRestResponse());
    }

    @PATCH
    @Path("/manage/password")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"SELF_UPSERT_PASSWORD"})
    public Uni<RestResponse<ApiResponse<Void>>> editUserPassword(@Valid EditUserPasswordRequest body) {
        return userDomain.updatePassword(UserEntity.builder().id(Types.toUUID(body.getId())).password(body.getPassword()).build(), body.getNewPassword()).map(result -> result.voidCast().toRestResponse());
    }

    @POST
    @Path("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_USER_DELETE", "USER_DELETE", "SELF_DELETE"})
    public Uni<RestResponse<ApiResponse<Void>>> deleteUser(@Valid DeleteRequest body) {
        if (!Types.validateUUID(body.getIds())) return Uni.createFrom().item(Result.<Void>badRequest().toRestResponse());
        return userDomain.delete(body.getIds().stream().map(id -> UserEntity.builder().id(Types.toUUID(id)).build()).toList()).map(result -> result.voidCast().toRestResponse());
    }
}
