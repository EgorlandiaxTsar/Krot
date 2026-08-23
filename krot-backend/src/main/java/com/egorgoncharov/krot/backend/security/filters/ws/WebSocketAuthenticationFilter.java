package com.egorgoncharov.krot.backend.security.filters.ws;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.security.crypto.Cipher;
import com.egorgoncharov.krot.backend.security.crypto.Generator;
import com.egorgoncharov.krot.backend.security.transport.TCPTransportManagerImpl;
import com.egorgoncharov.krot.backend.security.transport.headers.RequestHeaders;
import com.egorgoncharov.krot.backend.security.transport.session.RequestSession;
import io.smallrye.mutiny.Uni;
import io.vertx.core.buffer.Buffer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

@ApplicationScoped
public class WebSocketAuthenticationFilter {
    private final TCPTransportManagerImpl transportManager;

    @Inject
    public WebSocketAuthenticationFilter(TCPTransportManagerImpl transportManager) {
        this.transportManager = transportManager;
    }

    public Uni<Result<RequestSession>> authenticate(Buffer frame) {
        int tagIdx = 16; // UUID Length
        int nonceIdx = tagIdx + Cipher.TAG_LEN;
        int bodyIdx = nonceIdx + Generator.NONCE_LEN;
        ByteBuffer uuidByteBuffer = ByteBuffer.wrap(frame.getBytes(0, tagIdx));
        return transportManager.establishRequest(frame.getBuffer(bodyIdx, frame.length()), new RequestHeaders(
                Base64.getEncoder().encodeToString(frame.getBytes(tagIdx, nonceIdx)),
                Base64.getEncoder().encodeToString(frame.getBytes(nonceIdx, bodyIdx)),
                new UUID(uuidByteBuffer.getLong(), uuidByteBuffer.getLong()).toString()
        ));
    }
}
