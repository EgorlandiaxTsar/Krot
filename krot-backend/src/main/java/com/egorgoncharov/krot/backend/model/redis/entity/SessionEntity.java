package com.egorgoncharov.krot.backend.model.redis.entity;

import com.egorgoncharov.krot.backend.model.Identifiable;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SessionEntity implements Identifiable<UUID> {
    private UUID id;
    private UUID ownerId;
    private boolean isOwnerDevice;
    private UUID sessionReference;
    private byte[] handshakeKey;
    private byte[] encryptionKey;
    private OffsetDateTime lastUsed;
    private OffsetDateTime validUntil;
    private OffsetDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SessionEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
