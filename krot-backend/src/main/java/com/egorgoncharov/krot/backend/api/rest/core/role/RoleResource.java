package com.egorgoncharov.krot.backend.api.rest.core.role;

import com.egorgoncharov.krot.backend.api.rest.core.role.request.CreateRoleRequest;
import com.egorgoncharov.krot.backend.api.rest.core.role.request.EditRoleRequest;
import com.egorgoncharov.krot.backend.api.rest.core.role.request.GetRoleRequest;
import com.egorgoncharov.krot.backend.api.rest.core.role.response.Role;
import com.egorgoncharov.krot.backend.api.rest.request.DeleteRequest;
import com.egorgoncharov.krot.backend.api.rest.request.EntityReflection;
import com.egorgoncharov.krot.backend.api.rest.response.ApiResponse;
import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.application.core.role.RoleDomain;
import com.egorgoncharov.krot.backend.application.core.role.RoleQuery;
import com.egorgoncharov.krot.backend.application.query.pagination.Page;
import com.egorgoncharov.krot.backend.database.relational.entity.RoleEntity;
import com.egorgoncharov.krot.backend.security.Authority;
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

@Path("/api/role")
public class RoleResource {
    private final RoleDomain roleDomain;

    @Inject
    public RoleResource(RoleDomain roleDomain) {
        this.roleDomain = roleDomain;
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_ROLE_READ", "ROLE_READ", "SELF_READ"})
    public Uni<RestResponse<ApiResponse<List<EntityReflection<RoleEntity>>>>> filterRole(@Valid GetRoleRequest body) {
        if ((body.getAuthorities() != null && !Types.validateAuthorities(body.getAuthorities())) || (body.getIds() != null && !Types.validateUUID(body.getIds()))) return Uni.createFrom().item(Result.<List<EntityReflection<RoleEntity>>>badRequest().toRestResponse());
        return roleDomain.query(RoleQuery.builder()
                .authorities(body.getAuthorities().stream().map(Authority::valueOf).toList())
                .nameQuery(body.getNameQuery())
                .grade(body.getGrade())
                .ids(Types.toUUID(body.getIds()))
                .pagination(body.getPagination())
                .build()
        ).chain(result -> {
            if (result.getCode() != 200 || result.getResult().isEmpty()) return Uni.createFrom().item(result.<List<EntityReflection<RoleEntity>>>nullCast().toRestResponse());
            Page<RoleEntity> data = result.getResult().get();
            return Uni.createFrom().item(new ApiResponse<>(result.toApiMetadata(), data.getItems().stream().map(e -> new Role().from(e)).toList(), data).toRestResponse());
        });
    }

    @POST
    @Path("/manage")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_ROLE_UPSERT", "ROLE_UPSERT"})
    public Uni<RestResponse<ApiResponse<Void>>> createRole(@Valid CreateRoleRequest body) {
        if (body.getAuthorities() == null || !Types.validateAuthorities(body.getAuthorities())) return Uni.createFrom().item(Result.<Void>badRequest().toRestResponse());
        return roleDomain.create(RoleEntity.builder().name(body.getName()).grade(body.getGrade()).authorities(Types.toAuthoritiesList(body.getAuthorities())).build()).map(result -> result.voidCast().toRestResponse());
    }

    @PATCH
    @Path("/manage")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_ROLE_UPSERT", "ROLE_UPSERT"})
    public Uni<RestResponse<ApiResponse<Void>>> editRole(@Valid EditRoleRequest body) {
        if (body.getAuthorities() != null && !Types.validateAuthorities(body.getAuthorities())) return Uni.createFrom().item(Result.<Void>badRequest().toRestResponse());
        return roleDomain.update(RoleEntity.builder().id(Types.toUUID(body.getId())).name(body.getName()).grade(body.getGrade()).authorities(Types.toAuthoritiesList(body.getAuthorities())).build()).map(result -> result.voidCast().toRestResponse());
    }

    @POST
    @Path("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_ROLE_DELETE", "ROLE_DELETE"})
    public Uni<RestResponse<ApiResponse<Void>>> deleteRole(@Valid DeleteRequest body) {
        if (!Types.validateUUID(body.getIds())) return Uni.createFrom().item(Result.<Void>badRequest().toRestResponse());
        return roleDomain.delete(body.getIds().stream().map(id -> RoleEntity.builder().id(Types.toUUID(id)).build()).toList()).map(result -> result.voidCast().toRestResponse());
    }
}
