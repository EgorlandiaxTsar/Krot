package com.egorgoncharov.krot.backend.api.rest.core.program;

import com.egorgoncharov.krot.backend.api.rest.core.program.request.DeleteProgramCollaboratorRequest;
import com.egorgoncharov.krot.backend.api.rest.core.program.request.GetProgramRequest;
import com.egorgoncharov.krot.backend.api.rest.core.program.request.UpsertProgramCollaboratorRequest;
import com.egorgoncharov.krot.backend.api.rest.core.program.request.UpsertProgramRequest;
import com.egorgoncharov.krot.backend.api.rest.core.program.response.Program;
import com.egorgoncharov.krot.backend.api.rest.request.DeleteRequest;
import com.egorgoncharov.krot.backend.api.rest.request.EntityReflection;
import com.egorgoncharov.krot.backend.api.rest.request.TransferOwnershipRequest;
import com.egorgoncharov.krot.backend.api.rest.response.ApiResponse;
import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.application.core.program.ProgramDomain;
import com.egorgoncharov.krot.backend.application.core.program.ProgramQuery;
import com.egorgoncharov.krot.backend.application.query.pagination.Page;
import com.egorgoncharov.krot.backend.database.relational.entity.ProgramCollaboratorEntity;
import com.egorgoncharov.krot.backend.database.relational.entity.ProgramEntity;
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

@Path("/api/program")
public class ProgramResource {
    private final ProgramDomain programDomain;

    @Inject
    public ProgramResource(ProgramDomain programDomain) {
        this.programDomain = programDomain;
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_PROGRAM_READ", "PROGRAM_READ", "SELF_READ"})
    public Uni<RestResponse<ApiResponse<List<EntityReflection<ProgramEntity>>>>> filterProgram(@Valid GetProgramRequest body) {
        if (body.getIds() != null && !Types.validateUUID(body.getIds())) return Uni.createFrom().item(Result.<List<EntityReflection<ProgramEntity>>>badRequest().toRestResponse());
        return programDomain.query(ProgramQuery.builder()
                .creationTime(body.getCreationTime())
                .ownerId(Types.toUUID(body.getOwnerId()))
                .nameQuery(body.getNameQuery())
                .ids(Types.toUUID(body.getIds()))
                .pagination(body.getPagination())
                .build()
        ).chain(result -> {
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
        return programDomain.create(ProgramEntity.builder().name(body.getName()).build()).map(result -> result.voidCast().toRestResponse());
    }

    @PATCH
    @Path("/manage")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_PROGRAM_UPSERT", "PROGRAM_UPSERT", "SELF_UPSERT"})
    public Uni<RestResponse<ApiResponse<Void>>> editProgram(@Valid UpsertProgramRequest body) {
        return programDomain.update(ProgramEntity.builder().name(body.getName()).build()).map(result -> result.voidCast().toRestResponse());
    }

    @PATCH
    @Path("/manage/collaborator/upsert")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_PROGRAM_UPSERT", "PROGRAM_UPSERT", "SELF_UPSERT"})
    public Uni<RestResponse<ApiResponse<Void>>> upsertProgramCollaborator(@Valid UpsertProgramCollaboratorRequest body) {
        return programDomain.upsertCollaborator(ProgramEntity.builder()
                .id(Types.toUUID(body.getProgramId()))
                .collaborators(List.of(ProgramCollaboratorEntity.builder()
                        .collaborator(body.getUserId() == null ? null : UserEntity.builder().id(Types.toUUID(body.getUserId())).build())
                        .canUpdateName(body.getCanUpdateName())
                        .canUpdateCode(body.getCanUpdateCode())
                        .build()
                ))
                .build()
        ).map(result -> result.voidCast().toRestResponse());
    }

    @POST
    @Path("/manage/collaborator/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_PROGRAM_UPSERT", "PROGRAM_UPSERT", "SELF_UPSERT"})
    public Uni<RestResponse<ApiResponse<Void>>> deleteProgramCollaborator(@Valid DeleteProgramCollaboratorRequest body) {
        return programDomain.upsertCollaborator(ProgramEntity.builder()
                .id(Types.toUUID(body.getProgramId()))
                .collaborators(List.of(ProgramCollaboratorEntity.builder().id(Types.toUUID(body.getId())).build()))
                .build()
        ).map(result -> result.voidCast().toRestResponse());
    }

    @POST
    @Path("/transfer")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_PROGRAM_TRANSFER_OWNERSHIP", "PROGRAM_TRANSFER_OWNERSHIP", "SELF_PROGRAM_TRANSFER_OWNERSHIP"})
    public Uni<RestResponse<ApiResponse<Void>>> transferProgramOwnership(@Valid TransferOwnershipRequest body) {
        return programDomain.transferOwnership(ProgramEntity.builder()
                .id(Types.toUUID(body.getId()))
                .owner(UserEntity.builder().id(Types.toUUID(body.getNewOwnerId())).build())
                .build()
        ).map(result -> result.voidCast().toRestResponse());
    }

    @POST
    @Path("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(value = {"X_PROGRAM_DELETE", "PROGRAM_DELETE", "SELF_UPSERT"})
    public Uni<RestResponse<ApiResponse<Void>>> deleteProgram(@Valid DeleteRequest body) {
        if (!Types.validateUUID(body.getIds())) return Uni.createFrom().item(Result.<Void>badRequest().toRestResponse());
        return programDomain.delete(body.getIds().stream().map(id -> ProgramEntity.builder().id(Types.toUUID(id)).build()).toList()).map(result -> result.voidCast().toRestResponse());
    }
}
