package com.egorgoncharov.krot.backend.model.relational.entity;

import com.egorgoncharov.krot.backend.model.Identifiable;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "session")
public class HistoricalSessionEntity implements Identifiable<UUID> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_owner_id")
    private UserEntity userOwner;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_owner_id")
    private DeviceEntity deviceOwner;
    @Column(name = "valid_until", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime validUntil;
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HistoricalSessionEntity that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(userOwner, that.userOwner) && Objects.equals(deviceOwner, that.deviceOwner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userOwner, deviceOwner);
    }
}
