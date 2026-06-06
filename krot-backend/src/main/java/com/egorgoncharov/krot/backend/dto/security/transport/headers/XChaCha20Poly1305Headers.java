package com.egorgoncharov.krot.backend.dto.security.transport.headers;

import io.vertx.ext.web.RoutingContext;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Base64;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
public class XChaCha20Poly1305Headers {
    public static final String TAG_HEADER_NAME = "X-Request-Tag";
    public static final String NONCE_HEADER_NAME = "X-Request-Nonce";

    private String tag;
    private String nonce;

    public static XChaCha20Poly1305Headers fromRoutingContext(RoutingContext context) {
        return new XChaCha20Poly1305Headers(context.request().getHeader(TAG_HEADER_NAME), context.request().getHeader(NONCE_HEADER_NAME));
    }

    public boolean isComplete() {
        return tag != null && nonce != null;
    }

    public byte[] getTag() {
        return Base64.getDecoder().decode(tag);
    }

    public void setTag(byte[] tag) {
        this.tag = Base64.getEncoder().encodeToString(tag);
    }

    public byte[] getNonce() {
        return Base64.getDecoder().decode(nonce);
    }

    public void setNonce(byte[] nonce) {
        this.nonce = Base64.getEncoder().encodeToString(nonce);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof XChaCha20Poly1305Headers that)) return false;
        return Objects.equals(tag, that.tag) && Objects.equals(nonce, that.nonce);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, nonce);
    }
}
