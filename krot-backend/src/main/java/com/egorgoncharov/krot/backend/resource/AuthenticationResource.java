package com.egorgoncharov.krot.backend.resource;

import com.egorgoncharov.krot.backend.dto.api.ApiResponse;
import com.egorgoncharov.krot.backend.dto.api.request.RequestWithMetadata;
import com.egorgoncharov.krot.backend.dto.api.request.auth.AuthenticationHandshakeRequest;
import com.egorgoncharov.krot.backend.dto.api.response.AuthenticationCredentials;
import com.egorgoncharov.krot.backend.dto.security.auth.principal.PrincipalType;
import com.egorgoncharov.krot.backend.dto.security.auth.session.HandshakeSession;
import com.egorgoncharov.krot.backend.dto.service.Result;
import com.egorgoncharov.krot.backend.model.redis.entity.SessionEntity;
import com.egorgoncharov.krot.backend.service.helper.TypesHelper;
import com.egorgoncharov.krot.backend.service.impl.AuthenticationServiceImpl;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.Base64;

@Path("/api/auth")
public class AuthenticationResource {
    private final AuthenticationServiceImpl authenticationService;

    @Inject
    public AuthenticationResource(AuthenticationServiceImpl authenticationService) {
        this.authenticationService = authenticationService;
    }

    @POST
    @Path("/handshake")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<AuthenticationCredentials>>> authHandshake(RoutingContext context, @Valid AuthenticationHandshakeRequest handshakeCredentials) {
        HandshakeSession handshakeSession = context.get("handshakeSession");
        return authenticationService.login(SessionEntity.builder().isOwnerDevice(handshakeSession.getTarget() == PrincipalType.DEVICE).handshakeKey(handshakeSession.getKey()).build(), handshakeCredentials.getIdentifier(), handshakeCredentials.getPassword()).chain(result -> {
            if (result.getCode() != 200) return new ApiResponse<AuthenticationCredentials>(result.toApiMetadata(), null, null).toUniRestResponse();
            if (result.getResult().isEmpty()) return new ApiResponse<AuthenticationCredentials>(Result.error().toApiMetadata(), null, null).toUniRestResponse();
            SessionEntity session = result.getResult().get();
            return new ApiResponse<>(result.toApiMetadata(), new AuthenticationCredentials(session.getSessionReference().toString(), session.getId().toString(), Base64.getEncoder().encodeToString(session.getEncryptionKey()), session.getValidUntil().toInstant().toEpochMilli()), null).toUniRestResponse();
        });
    }

    @POST
    @Path("/disconnect")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<Void>>> authDisconnect(@Valid RequestWithMetadata body) {
        return authenticationService.logout(TypesHelper.toUUID(body.getMetadata().getSessionId())).map(result -> result.voidCast().toRestResponse());
    }
}
