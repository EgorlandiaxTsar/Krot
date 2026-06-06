package com.egorgoncharov.krot.backend.dto.security.transport.headers;

import io.vertx.ext.web.RoutingContext;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Base64;

@AllArgsConstructor
@NoArgsConstructor
public class HandshakeHeaders extends XChaCha20Poly1305Headers {
    public static final String CLIENT_PUBKEY_HEADER_NAME = "X-Key";

    private String clientPubkey;

    public HandshakeHeaders(String tag, String nonce, String clientPubkey) {
        super(tag, nonce);
        this.clientPubkey = clientPubkey;
    }

    public static HandshakeHeaders fromRoutingContext(RoutingContext context) {
        return new HandshakeHeaders(context.request().getHeader(TAG_HEADER_NAME), context.request().getHeader(NONCE_HEADER_NAME), context.request().getHeader(CLIENT_PUBKEY_HEADER_NAME));
    }

    public byte[] getClientPubkey() {
        return Base64.getDecoder().decode(clientPubkey);
    }

    public void setClientPubkey(byte[] clientPubkey) {
        this.clientPubkey = Base64.getEncoder().encodeToString(clientPubkey);
    }

    @Override
    public boolean isComplete() {
        return super.isComplete() && clientPubkey != null;
    }
}
