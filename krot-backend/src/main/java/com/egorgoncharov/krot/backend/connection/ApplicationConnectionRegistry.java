package com.egorgoncharov.krot.backend.connection;

import com.egorgoncharov.krot.backend.database.relational.entity.RoleEntity;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Startup
@ApplicationScoped
public class ApplicationConnectionRegistry implements ConnectionRegistry {
    // TODO: Consider using a different data structure with O(1) computational complexity for accessing and replacing data, not necessary for small admin counts (up to 10000 entities)
    private final Map<UUID, ConnectionMetadata> connections = new ConcurrentHashMap<>();

    @Override
    public void addConnection(ConnectionMetadata metadata) {
        ConnectionMetadata existingMetadata = connections.get(metadata.getUserId());
        existingMetadata = existingMetadata == null ? metadata : existingMetadata;
        existingMetadata.incrementReference();
        connections.put(metadata.getUserId(), metadata);
    }

    @Override
    public ConnectionMetadata getConnection(UUID userId) {
        return ConnectionMetadata.from(connections.get(userId));
    }

    @Override
    public void removeConnection(UUID userId) {
        ConnectionMetadata existingMetadata = connections.get(userId);
        if (existingMetadata == null) return;
        if (existingMetadata.decrementReference()) connections.remove(userId);
    }

    @Override
    public void updateConnection(ConnectionMetadata metadata) {
        connections.replace(metadata.getUserId(), metadata);
    }

    @Override
    public Map<UUID, ConnectionMetadata> getConnections() {
        return new ConcurrentHashMap<>(connections);
    }

    protected void updateRoles(RoleEntity role) {
        connections.replaceAll((userId, metadata) -> metadata.getRoleId().equals(role.getId()) ? ConnectionMetadata.from(userId, role) : metadata);
    }
}
