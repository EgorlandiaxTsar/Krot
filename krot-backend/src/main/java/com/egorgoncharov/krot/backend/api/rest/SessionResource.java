package com.egorgoncharov.krot.backend.api.rest;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.api.model.request.EntityReflection;
import com.egorgoncharov.krot.backend.api.model.request.session.GetSessionRequest;
import com.egorgoncharov.krot.backend.api.model.response.ApiResponse;
import com.egorgoncharov.krot.backend.api.model.response.session.Session;
import com.egorgoncharov.krot.backend.application.domain.SessionDomain;
import com.egorgoncharov.krot.backend.application.query.model.SessionQuery;
import com.egorgoncharov.krot.backend.application.query.pagination.Page;
import com.egorgoncharov.krot.backend.database.relational.entity.HistoricalSessionEntity;
import com.egorgoncharov.krot.backend.util.Types;
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
    private final SessionDomain sessionDomain;

    @Inject
    public SessionResource(SessionDomain sessionDomain) {
        this.sessionDomain = sessionDomain;
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<List<EntityReflection<HistoricalSessionEntity>>>>> filterSession(@Valid GetSessionRequest body) {
        if (body.getIds() != null && !Types.validateUUID(body.getIds())) return Uni.createFrom().item(Result.<List<EntityReflection<HistoricalSessionEntity>>>badRequest().toRestResponse());
        return sessionDomain.query(SessionQuery.builder()
                .ownerId(Types.toUUID(body.getOwnerId()))
                .expirationTime(body.getValidUntilTime())
                .creationTime(body.getCreationTime())
                .ids(Types.toUUID(body.getIds()))
                .pagination(body.getPagination())
                .build()
        ).chain(result -> {
            if (result.getCode() != 200 || result.getResult().isEmpty()) return Uni.createFrom().item(result.<List<EntityReflection<HistoricalSessionEntity>>>nullCast().toRestResponse());
            Page<HistoricalSessionEntity> data = result.getResult().get();
            return Uni.createFrom().item(new ApiResponse<>(result.toApiMetadata(), data.getItems().stream().map(e -> new Session().from(e)).toList(), data).toRestResponse());
        });
    }
}
