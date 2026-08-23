package com.egorgoncharov.krot.backend.connection;

import com.egorgoncharov.krot.backend.database.relational.entity.RoleEntity;
import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
import com.egorgoncharov.krot.backend.security.Authority;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class ConnectionMetadata {
    private final UUID userId;
    private final UUID roleId;
    private final int roleGrade;
    private final Set<Authority> authorities;

    private int referencesCount = 0;

    private ConnectionMetadata(UUID userId, UUID roleId, int roleGrade, Set<Authority> authorities) {
        this.userId = userId;
        this.roleId = roleId;
        this.roleGrade = roleGrade;
        this.authorities = Set.copyOf(authorities);
    }

    public static ConnectionMetadata from(UUID userId, RoleEntity role) {
        return new ConnectionMetadata(userId, role.getId(), role.getGrade(), new HashSet<>(role.getAuthorities()));
    }

    public static ConnectionMetadata from(UserEntity user) {
        return from(user.getId(), user.getRole());
    }

    public static ConnectionMetadata from(ConnectionMetadata connectionMetadata) {
        return new ConnectionMetadata(connectionMetadata.userId, connectionMetadata.roleId, connectionMetadata.roleGrade, connectionMetadata.authorities);
    }

    public void incrementReference() {
        this.referencesCount++;
    }

    public boolean decrementReference() {
        return ++this.referencesCount <= 0; // Returns true if metadata is fully unlinked
    }
}
