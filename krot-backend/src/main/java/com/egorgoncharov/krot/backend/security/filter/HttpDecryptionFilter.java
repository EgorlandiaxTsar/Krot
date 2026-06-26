package com.egorgoncharov.krot.backend.security.filter;

import com.egorgoncharov.krot.backend.dto.security.auth.session.HandshakeSession;
import com.egorgoncharov.krot.backend.dto.security.transport.headers.HandshakeHeaders;
import com.egorgoncharov.krot.backend.dto.security.transport.headers.RequestHeaders;
import com.egorgoncharov.krot.backend.dto.service.Result;
import com.egorgoncharov.krot.backend.service.impl.TCPTransportServiceImpl;
import io.smallrye.mutiny.Uni;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;

import java.io.ByteArrayInputStream;

public class HttpDecryptionFilter {
    private final TCPTransportServiceImpl tcpTransportService;

    @Inject
    public HttpDecryptionFilter(TCPTransportServiceImpl tcpTransportService) {
        this.tcpTransportService = tcpTransportService;
    }

    @ServerRequestFilter(preMatching = true)
    public Uni<Response> decrypt(ContainerRequestContext context, RoutingContext vertxContext) {
        String path = context.getUriInfo().getPath();
        if (path.equals("/hello") || path.equals("/pubkey")) return Uni.createFrom().nullItem();
        byte[] originalBody = readBodyBytes(context);
        if (path.equals("/api/auth/handshake")) {
            Result<HandshakeSession> result = tcpTransportService.establishHandshakeRequest(Buffer.buffer(originalBody), HandshakeHeaders.fromRoutingContext(vertxContext));
            if (result.getCode() == 200 && result.getResult().isPresent()) {
                HandshakeSession session = result.getResult().get();
                vertxContext.put("handshakeSession", session);
                context.getHeaders().putSingle("Content-Type", MediaType.APPLICATION_JSON);
                context.setEntityStream(new ByteArrayInputStream(session.getBody()));
            } else {
                return Uni.createFrom().item(Response.status(result.getCode()).build());
            }
        } else {
            return tcpTransportService.establishRequest(Buffer.buffer(originalBody), RequestHeaders.fromRoutingContext(vertxContext)).chain(result -> {
                if (result.getCode() == 200 && result.getResult().isPresent()) {
                    vertxContext.put("requestSession", result.getResult().get());
                    context.setEntityStream(new ByteArrayInputStream(result.getResult().get().getBody()));
                    context.getHeaders().putSingle("Content-Type", MediaType.APPLICATION_JSON);
                    return Uni.createFrom().nullItem();
                } else {
                    return Uni.createFrom().item(Response.status(result.getCode()).build());
                }
            });
        }
        return Uni.createFrom().nullItem();
    }

    private byte[] readBodyBytes(ContainerRequestContext context) {
        try {
            return context.getEntityStream().readAllBytes();
        } catch (Exception e) {
            return new byte[0];
        }
    }
}
