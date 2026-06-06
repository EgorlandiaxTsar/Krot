package com.egorgoncharov.krot.backend.model.entity;

import com.egorgoncharov.krot.backend.model.common.Identifiable;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "session")
public class SessionEntity implements Identifiable<UUID> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_owner_id")
    private UserEntity userOwner;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_owner_id")
    private DeviceEntity deviceOwner;
    @Column(name = "src_key", nullable = false, unique = true)
    private UUID srcKey;
    @Column(name = "handshake_key", nullable = false, length = 256)
    private byte[] handshakeKey;
    @Column(name = "encryption_key", nullable = false, length = 256)
    private byte[] encryptionKey;
    @Column(name = "last_used", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime lastUsed;
    @Column(name = "valid_until", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime validUntil;
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SessionEntity that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(userOwner == null ? deviceOwner.getId() : userOwner.getId(), that.userOwner == null ? that.deviceOwner.getId() : that.userOwner.getId()) && Arrays.equals(handshakeKey, that.handshakeKey) && Arrays.equals(encryptionKey, that.encryptionKey) && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userOwner == null ? deviceOwner.getId() : userOwner.getId(), Arrays.hashCode(handshakeKey), Arrays.hashCode(encryptionKey), createdAt);
    }
}
