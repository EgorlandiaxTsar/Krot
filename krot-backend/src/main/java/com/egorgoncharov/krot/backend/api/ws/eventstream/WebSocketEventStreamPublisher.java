package com.egorgoncharov.krot.backend.api.ws.eventstream;

import com.egorgoncharov.krot.backend.api.model.response.ApiEvent;
import com.egorgoncharov.krot.backend.api.ws.WebSocketUserDataKeys;
import com.egorgoncharov.krot.backend.database.Identifiable;
import com.egorgoncharov.krot.backend.security.filters.ws.WebSocketEncryptionFilter;
import com.egorgoncharov.krot.backend.security.session.ws.WebSocketSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.websockets.next.OpenConnections;
import io.vertx.core.buffer.Buffer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class WebSocketEventStreamPublisher {
    private final OpenConnections openConnections;
    private final ObjectMapper objectMapper;
    private final WebSocketEncryptionFilter encryptionFilter;

    @Inject
    public WebSocketEventStreamPublisher(OpenConnections openConnections, ObjectMapper objectMapper, WebSocketEncryptionFilter encryptionFilter) {
        this.openConnections = openConnections;
        this.objectMapper = objectMapper;
        this.encryptionFilter = encryptionFilter;
    }

    public <T extends Identifiable<I>, I, A> void push(ApiEvent<T, I, A> event, List<UUID> userIds) {
        if (userIds.isEmpty()) return;
        byte[] body;
        try {
            body = objectMapper.writeValueAsBytes(event);
        } catch (JsonProcessingException e) {
            // TODO: Handle error gracefully
            return;
        }
        openConnections.listAll().stream()
                .filter(connection -> {
                    WebSocketSession session = connection.userData().get(WebSocketUserDataKeys.WS_SESSION);
                    return session != null && userIds.contains(session.getUserId());
                })
                .forEach(connection -> {
                    WebSocketSession session = connection.userData().get(WebSocketUserDataKeys.WS_SESSION);
                    if (session == null) return;
                    Buffer frame = encryptionFilter.encryptFrame(body, session.getSession().getSession().getEncryptionKey());
                    connection.sendBinary(frame).subscribe().with(
                            ignored -> {
                            },
                            failure -> {
                                // TODO: Handle error gracefully
                            }
                    );
                });
    }
}
