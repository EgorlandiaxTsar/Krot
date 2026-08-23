package com.egorgoncharov.krot.backend.security.filters.ws;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.security.transport.TCPTransportManagerImpl;
import com.egorgoncharov.krot.backend.security.transport.response.EncryptedResponse;
import io.vertx.core.buffer.Buffer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Base64;

@ApplicationScoped
public class WebSocketEncryptionFilter {
    private final TCPTransportManagerImpl transportManager;

    @Inject
    public WebSocketEncryptionFilter(TCPTransportManagerImpl transportManager) {
        this.transportManager = transportManager;
    }

    public Buffer encryptFrame(byte[] body, byte[] key) {
        Result<EncryptedResponse> result = transportManager.encryptResponse(body, key);
        if (result.getCode() != 200 || result.getResult().isEmpty()) {
            throw new IllegalStateException("WS frame encryption failed");
        }
        EncryptedResponse encryptedResponse = result.getResult().get();
        Buffer out = Buffer.buffer();
        out.appendBytes(Base64.getDecoder().decode(encryptedResponse.tag()));
        out.appendBytes(Base64.getDecoder().decode(encryptedResponse.nonce()));
        out.appendBytes(encryptedResponse.body());
        return out;
    }
}
