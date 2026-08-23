package com.egorgoncharov.krot.backend.connection;

import java.util.Map;
import java.util.UUID;

public interface ConnectionRegistry {
    void addConnection(ConnectionMetadata metadata);

    ConnectionMetadata getConnection(UUID userId);

    void removeConnection(UUID userId);

    void updateConnection(ConnectionMetadata metadata);

    Map<UUID, ConnectionMetadata> getConnections();
}
