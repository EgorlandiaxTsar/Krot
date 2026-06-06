package com.egorgoncharov.krot.backend.service;

import com.egorgoncharov.krot.backend.dto.security.auth.session.HandshakeSession;
import com.egorgoncharov.krot.backend.dto.security.auth.session.RequestSession;
import com.egorgoncharov.krot.backend.dto.security.transport.EncryptedResponse;
import com.egorgoncharov.krot.backend.dto.security.transport.headers.HandshakeHeaders;
import com.egorgoncharov.krot.backend.dto.security.transport.headers.RequestHeaders;
import com.egorgoncharov.krot.backend.dto.service.Result;
import io.smallrye.mutiny.Uni;
import io.vertx.core.buffer.Buffer;

public interface TCPTransportService {
    Result<HandshakeSession> establishHandshakeRequest(Buffer body, HandshakeHeaders headers);

    Uni<Result<RequestSession>> establishRequest(Buffer body, RequestHeaders headers);

    Result<EncryptedResponse> encryptResponse(byte[] body, byte[] key);
}
