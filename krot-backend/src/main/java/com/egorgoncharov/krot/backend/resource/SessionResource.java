package com.egorgoncharov.krot.backend.resource;

import com.egorgoncharov.krot.backend.dto.api.ApiResponse;
import com.egorgoncharov.krot.backend.dto.api.request.EntityReflection;
import com.egorgoncharov.krot.backend.dto.api.request.session.GetSessionRequest;
import com.egorgoncharov.krot.backend.dto.api.response.Session;
import com.egorgoncharov.krot.backend.dto.service.Result;
import com.egorgoncharov.krot.backend.dto.service.pagination.Page;
import com.egorgoncharov.krot.backend.model.relational.entity.HistoricalSessionEntity;
import com.egorgoncharov.krot.backend.service.helper.TypesHelper;
import com.egorgoncharov.krot.backend.service.impl.HistoricalSessionServiceImpl;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.List;

@Path("/api/session")
public class SessionResource {
    private final HistoricalSessionServiceImpl historicalSessionService;

    @Inject
    public SessionResource(HistoricalSessionServiceImpl historicalSessionService) {
        this.historicalSessionService = historicalSessionService;
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<List<EntityReflection<HistoricalSessionEntity>>>>> filterSession(@Valid GetSessionRequest body) {
        if (body.getIds() != null && !TypesHelper.validateUUID(body.getIds())) return Uni.createFrom().item(Result.<List<EntityReflection<HistoricalSessionEntity>>>badRequest().toRestResponse());
        return historicalSessionService.filter(TypesHelper.toUUID(body.getIds()), TypesHelper.toUUID(body.getOwnerId()), TypesHelper.toUUID(body.getOwnerId()), body.getValidUntilTime(), body.getCreationTime(), body.getPagination()).chain(result -> {
            if (result.getCode() != 200 || result.getResult().isEmpty()) return Uni.createFrom().item(result.<List<EntityReflection<HistoricalSessionEntity>>>nullCast().toRestResponse());
            Page<HistoricalSessionEntity> data = result.getResult().get();
            return Uni.createFrom().item(new ApiResponse<>(result.toApiMetadata(), data.getItems().stream().map(e -> new Session().from(e)).toList(), data).toRestResponse());
        });
    }
}
