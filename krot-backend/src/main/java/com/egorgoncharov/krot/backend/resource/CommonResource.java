package com.egorgoncharov.krot.backend.resource;

import com.egorgoncharov.krot.backend.config.yaml.HandshakeConfig;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class CommonResource {
    private final HandshakeConfig handshakeConfig;

    @Inject
    public CommonResource(HandshakeConfig handshakeConfig) {
        this.handshakeConfig = handshakeConfig;
    }

    @GET
    @Path("hello")
    @Produces(MediaType.TEXT_PLAIN)
    @PermitAll
    public String hello() {
        return "Hello!";
    }

    @GET
    @Path("pubkey")
    @Produces(MediaType.TEXT_PLAIN)
    @PermitAll
    public String pubkey() {
        return handshakeConfig.publicKey();
    }
}
