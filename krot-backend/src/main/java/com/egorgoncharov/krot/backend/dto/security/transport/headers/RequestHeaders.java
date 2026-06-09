package com.egorgoncharov.krot.backend.dto.security.transport.headers;

import com.egorgoncharov.krot.backend.service.helper.TypesHelper;
import io.vertx.ext.web.RoutingContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RequestHeaders extends XChaCha20Poly1305Headers {
    public static final String SESSION_REFERENCE_HEADER_NAME = "X-Session-Reference";

    private String sessionReference;

    public RequestHeaders(String tag, String nonce, String sessionReference) {
        super(tag, nonce);
        this.sessionReference = sessionReference;
    }

    public static RequestHeaders fromRoutingContext(RoutingContext context) {
        return new RequestHeaders(context.request().getHeader(TAG_HEADER_NAME), context.request().getHeader(NONCE_HEADER_NAME), context.request().getHeader(SESSION_REFERENCE_HEADER_NAME));
    }

    @Override
    public boolean isComplete() {
        return super.isComplete() && sessionReference != null && TypesHelper.validateUUID(sessionReference);
    }
}
