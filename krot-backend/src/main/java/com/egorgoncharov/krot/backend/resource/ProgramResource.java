package com.egorgoncharov.krot.backend.resource;

import com.egorgoncharov.krot.backend.dto.api.ApiResponse;
import com.egorgoncharov.krot.backend.dto.api.request.DeleteRequest;
import com.egorgoncharov.krot.backend.dto.api.request.EntityReflection;
import com.egorgoncharov.krot.backend.dto.api.request.TransferOwnershipRequest;
import com.egorgoncharov.krot.backend.dto.api.request.program.GetProgramRequest;
import com.egorgoncharov.krot.backend.dto.api.request.program.UpsertProgramRequest;
import com.egorgoncharov.krot.backend.dto.api.response.Program;
import com.egorgoncharov.krot.backend.dto.service.Result;
import com.egorgoncharov.krot.backend.dto.service.pagination.Page;
import com.egorgoncharov.krot.backend.model.entity.ProgramEntity;
import com.egorgoncharov.krot.backend.service.helper.TypesHelper;
import com.egorgoncharov.krot.backend.service.impl.ProgramServiceImpl;
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

@Path("/api/program")
public class ProgramResource {
    private final ProgramServiceImpl programService;

    @Inject
    public ProgramResource(ProgramServiceImpl programService) {
        this.programService = programService;
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_PROGRAM_READ", "PROGRAM_READ", "SELF_READ"})
    public Uni<RestResponse<ApiResponse<List<EntityReflection<ProgramEntity>>>>> filterProgram(@Valid GetProgramRequest body) {
        if (body.getIds() != null && !TypesHelper.validateUUID(body.getIds())) return Uni.createFrom().item(Result.<List<EntityReflection<ProgramEntity>>>badRequest().toRestResponse());
        return programService.filter(TypesHelper.toUUID(body.getIds()), body.getCreationTime(), TypesHelper.toUUID(body.getOwnerId()), body.getNameQuery(), body.getPagination()).chain(result -> {
            if (result.getCode() != 200 || result.getResult().isEmpty()) return Uni.createFrom().item(result.<List<EntityReflection<ProgramEntity>>>nullCast().toRestResponse());
            Page<ProgramEntity> data = result.getResult().get();
            return Uni.createFrom().item(new ApiResponse<>(result.toApiMetadata(), data.getItems().stream().map(e -> new Program().from(e)).toList(), data).toRestResponse());
        });
    }

    @POST
    @Path("/manage")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_PROGRAM_UPSERT", "PROGRAM_UPSERT", "SELF_UPSERT"})
    public Uni<RestResponse<ApiResponse<Void>>> createProgram(@Valid UpsertProgramRequest body) {
        return programService.create(ProgramEntity.builder().name(body.getName()).build()).map(result -> result.voidCast().toRestResponse());
    }

    @PATCH
    @Path("/manage")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_PROGRAM_UPSERT", "PROGRAM_UPSERT", "SELF_UPSERT"})
    public Uni<RestResponse<ApiResponse<Void>>> editProgram(@Valid UpsertProgramRequest body) {
        return programService.update(ProgramEntity.builder().name(body.getName()).build()).map(result -> result.voidCast().toRestResponse());
    }

    @POST
    @Path("/transfer")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_PROGRAM_TRANSFER_OWNERSHIP", "PROGRAM_TRANSFER_OWNERSHIP", "SELF_PROGRAM_TRANSFER_OWNERSHIP"})
    public Uni<RestResponse<ApiResponse<Void>>> transferDeviceOwnership(@Valid TransferOwnershipRequest body) {
        return programService.transferOwnership(TypesHelper.toUUID(body.getId()), TypesHelper.toUUID(body.getNewOwnerId())).map(result -> result.voidCast().toRestResponse());
    }

    @POST
    @Path("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_PROGRAM_DELETE", "PROGRAM_DELETE", "SELF_UPSERT"})
    public Uni<RestResponse<ApiResponse<Void>>> deleteDevice(@Valid DeleteRequest body) {
        if (!TypesHelper.validateUUID(body.getIds())) return Uni.createFrom().item(Result.<Void>badRequest().toRestResponse());
        return programService.deleteById(TypesHelper.toUUID(body.getIds())).map(result -> result.voidCast().toRestResponse());
    }
}
