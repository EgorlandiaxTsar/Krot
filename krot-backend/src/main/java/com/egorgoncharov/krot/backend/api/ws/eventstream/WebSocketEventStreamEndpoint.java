package com.egorgoncharov.krot.backend.api.ws.eventstream;

import com.egorgoncharov.krot.backend.api.ws.WebSocketUserDataKeys;
import com.egorgoncharov.krot.backend.connection.ApplicationConnectionRegistry;
import com.egorgoncharov.krot.backend.connection.ConnectionMetadata;
import com.egorgoncharov.krot.backend.database.relational.repository.UserRepository;
import com.egorgoncharov.krot.backend.security.filters.ws.WebSocketAuthenticationFilter;
import com.egorgoncharov.krot.backend.security.filters.ws.WebSocketDecryptionFilter;
import com.egorgoncharov.krot.backend.security.filters.ws.WebSocketEncryptionFilter;
import com.egorgoncharov.krot.backend.security.session.ws.WebSocketSession;
import com.egorgoncharov.krot.backend.security.transport.session.RequestSession;
import io.quarkus.websockets.next.OnBinaryMessage;
import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import io.smallrye.mutiny.Uni;
import io.vertx.core.buffer.Buffer;
import jakarta.inject.Inject;
import org.bouncycastle.crypto.InvalidCipherTextException;


// TODO: Consider moving the websocket authentication flow logic in a different place to keep it reusable for other WS routes
@WebSocket(path = "/wsapi/events")
public class WebSocketEventStreamEndpoint {
    private final UserRepository userRepository;
    private final ApplicationConnectionRegistry connectionRegistry;
    private final WebSocketAuthenticationFilter authenticationFilter;
    private final WebSocketDecryptionFilter decryptionFilter;
    private final WebSocketEncryptionFilter encryptionFilter;

    @Inject
    public WebSocketEventStreamEndpoint(UserRepository userRepository, ApplicationConnectionRegistry connectionRegistry, WebSocketAuthenticationFilter authenticationFilter, WebSocketDecryptionFilter decryptionFilter, WebSocketEncryptionFilter encryptionFilter) {
        this.userRepository = userRepository;
        this.connectionRegistry = connectionRegistry;
        this.authenticationFilter = authenticationFilter;
        this.decryptionFilter = decryptionFilter;
        this.encryptionFilter = encryptionFilter;
    }

    @OnBinaryMessage
    public Uni<Buffer> onMessage(Buffer frame, WebSocketConnection connection) {
        WebSocketSession cached = connection.userData().get(WebSocketUserDataKeys.WS_SESSION);
        if (cached.getSession() == null) {
            return authenticationFilter.authenticate(frame).flatMap(result -> {
                if (result.getCode() != 200 || result.getResult().isEmpty()) {
                    return connection.close().replaceWith(Buffer.buffer()).replaceWithNull();
                }
                RequestSession session = result.getResult().get();
                return userRepository.findById(session.getSession().getOwnerId()).map(owner -> {
                    if (owner == null) {
                        throw new IllegalStateException("WS authenticated user not found");
                    }
                    connection.userData().put(WebSocketUserDataKeys.WS_SESSION, new WebSocketSession(owner.getId(), session));
                    connectionRegistry.addConnection(ConnectionMetadata.from(owner));
                    return encryptionFilter.encryptFrame(new byte[]{1}, session.getSession().getEncryptionKey());
                });
            });
        }
        // Echo
        byte[] key = cached.getSession().getSession().getEncryptionKey();
        return Uni.createFrom().item(frame)
                .map(f -> {
                    try {
                        return decryptionFilter.decrypt(f, key);
                    } catch (InvalidCipherTextException e) {
                        throw new RuntimeException(e);
                    } // TODO: Add more robust error handling
                })
                .map(body -> encryptionFilter.encryptFrame(body, key));
    }

    @OnClose
    public void onClose(WebSocketConnection connection) {
        WebSocketSession session = connection.userData().get(WebSocketUserDataKeys.WS_SESSION);
        if (session == null) return;
        connectionRegistry.removeConnection(session.getSession().getSession().getOwnerId());
    }
}
