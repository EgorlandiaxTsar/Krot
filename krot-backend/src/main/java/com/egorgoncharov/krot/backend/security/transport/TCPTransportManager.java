package com.egorgoncharov.krot.backend.security.transport;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.security.transport.headers.HandshakeHeaders;
import com.egorgoncharov.krot.backend.security.transport.headers.RequestHeaders;
import com.egorgoncharov.krot.backend.security.transport.response.EncryptedResponse;
import com.egorgoncharov.krot.backend.security.transport.session.HandshakeSession;
import com.egorgoncharov.krot.backend.security.transport.session.RequestSession;
import io.smallrye.mutiny.Uni;
import io.vertx.core.buffer.Buffer;

public interface TCPTransportManager {
    Result<HandshakeSession> establishHandshakeRequest(Buffer body, HandshakeHeaders headers);

    Uni<Result<RequestSession>> establishRequest(Buffer body, RequestHeaders headers);

    Result<EncryptedResponse> encryptResponse(byte[] body, byte[] key);
}
