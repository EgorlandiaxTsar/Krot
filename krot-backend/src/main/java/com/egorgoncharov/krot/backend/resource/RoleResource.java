package com.egorgoncharov.krot.backend.resource;

import com.egorgoncharov.krot.backend.dto.api.ApiResponse;
import com.egorgoncharov.krot.backend.dto.api.request.DeleteRequest;
import com.egorgoncharov.krot.backend.dto.api.request.EntityReflection;
import com.egorgoncharov.krot.backend.dto.api.request.role.CreateRoleRequest;
import com.egorgoncharov.krot.backend.dto.api.request.role.EditRoleRequest;
import com.egorgoncharov.krot.backend.dto.api.request.role.GetRoleRequest;
import com.egorgoncharov.krot.backend.dto.api.response.Role;
import com.egorgoncharov.krot.backend.dto.service.Result;
import com.egorgoncharov.krot.backend.dto.service.pagination.Page;
import com.egorgoncharov.krot.backend.model.relational.entity.RoleEntity;
import com.egorgoncharov.krot.backend.service.helper.TypesHelper;
import com.egorgoncharov.krot.backend.service.impl.RoleServiceImpl;
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
    private final RoleServiceImpl roleService;

    @Inject
    public RoleResource(RoleServiceImpl roleService) {
        this.roleService = roleService;
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_ROLE_READ", "ROLE_READ", "SELF_READ"})
    public Uni<RestResponse<ApiResponse<List<EntityReflection<RoleEntity>>>>> filterRole(@Valid GetRoleRequest body) {
        if ((body.getAuthorities() != null && !TypesHelper.validateAuthorities(body.getAuthorities())) || (body.getIds() != null && !TypesHelper.validateUUID(body.getIds()))) return Uni.createFrom().item(Result.<List<EntityReflection<RoleEntity>>>badRequest().toRestResponse());
        return roleService.filter(TypesHelper.toUUID(body.getIds()), body.getAuthorities(), body.getNameQuery(), body.getGrade(), body.getPagination()).chain(result -> {
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
        if (body.getAuthorities() == null || !TypesHelper.validateAuthorities(body.getAuthorities())) return Uni.createFrom().item(Result.<Void>badRequest().toRestResponse());
        return roleService.create(RoleEntity.builder().name(body.getName()).grade(body.getGrade()).authorities(TypesHelper.toAuthoritiesList(body.getAuthorities())).build()).map(result -> result.voidCast().toRestResponse());
    }

    @PATCH
    @Path("/manage")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_ROLE_UPSERT", "ROLE_UPSERT"})
    public Uni<RestResponse<ApiResponse<Void>>> editRole(@Valid EditRoleRequest body) {
        if (body.getAuthorities() != null && !TypesHelper.validateAuthorities(body.getAuthorities())) return Uni.createFrom().item(Result.<Void>badRequest().toRestResponse());
        return roleService.update(RoleEntity.builder().id(TypesHelper.toUUID(body.getId())).name(body.getName()).grade(body.getGrade()).authorities(TypesHelper.toAuthoritiesList(body.getAuthorities())).build()).map(result -> result.voidCast().toRestResponse());
    }

    @POST
    @Path("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_ROLE_DELETE", "ROLE_DELETE"})
    public Uni<RestResponse<ApiResponse<Void>>> deleteRole(@Valid DeleteRequest body) {
        if (!TypesHelper.validateUUID(body.getIds())) return Uni.createFrom().item(Result.<Void>badRequest().toRestResponse());
        return roleService.deleteById(TypesHelper.toUUID(body.getIds())).map(result -> result.voidCast().toRestResponse());
    }
}
