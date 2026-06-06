package com.egorgoncharov.krot.backend.security.filter;

import com.egorgoncharov.krot.backend.config.yaml.STPConfig;
import com.egorgoncharov.krot.backend.dto.security.auth.session.HandshakeSession;
import com.egorgoncharov.krot.backend.dto.security.auth.session.RequestSession;
import com.egorgoncharov.krot.backend.dto.security.transport.EncryptedResponse;
import com.egorgoncharov.krot.backend.dto.service.Result;
import com.egorgoncharov.krot.backend.service.impl.TCPTransportServiceImpl;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerResponseContext;
import org.jboss.resteasy.reactive.server.ServerResponseFilter;

public class HttpEncryptionFilter {
    public static final String TAG_HEADER = "X-Request-Tag";
    public static final String NONCE_HEADER = "X-Request-Nonce";

    private final STPConfig stpConfig;
    private final TCPTransportServiceImpl tcpTransportService;

    @Inject
    public HttpEncryptionFilter(STPConfig stpConfig, TCPTransportServiceImpl tcpTransportService) {
        this.stpConfig = stpConfig;
        this.tcpTransportService = tcpTransportService;
    }

    @ServerResponseFilter
    public void encrypt(ContainerResponseContext context, RoutingContext vertxContext) {
        Object entity = context.getEntity();
        if (entity == null) return;
        byte[] body = entity instanceof byte[] ? (byte[]) entity : entity.toString().getBytes();
        HandshakeSession handshakeSession = vertxContext.get("handshakeSession");
        RequestSession requestSession = vertxContext.get("requestSession");
        if (handshakeSession == null && requestSession == null) return;
        if (!stpConfig.enableOutcoming() && requestSession != null) return;
        byte[] key = handshakeSession != null ? handshakeSession.getKey() : requestSession.getSession().getEncryptionKey();
        Result<EncryptedResponse> result = tcpTransportService.encryptResponse(body, key);
        if (result.getCode() != 200 || result.getResult().isEmpty()) return;
        EncryptedResponse encryptedResponse = result.getResult().get();
        context.setEntity(encryptedResponse.getBody());
        context.getHeaders().add(TAG_HEADER, encryptedResponse.getTag());
        context.getHeaders().add(NONCE_HEADER, encryptedResponse.getNonce());
    }
}
