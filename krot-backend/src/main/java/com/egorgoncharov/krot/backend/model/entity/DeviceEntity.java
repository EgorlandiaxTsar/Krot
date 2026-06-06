package com.egorgoncharov.krot.backend.model.entity;

import com.egorgoncharov.krot.backend.model.common.Identifiable;
import com.egorgoncharov.krot.backend.model.common.Nameable;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@RegisterForReflection
@Entity
@Table(name = "device")
public class DeviceEntity implements Identifiable<UUID>, Nameable {
    @Id
    private UUID id;
    @Column(name = "name", nullable = false, unique = true, length = 128)
    private String name;
    @Column(name = "address", length = 16)
    private String address;
    @Column(name = "password", nullable = false, length = 32)
    private String password;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    private UserEntity owner;
    @Column(name = "last_call", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime lastCalled;
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime createdAt;
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "device")
    private List<DeviceCollaboratorEntity> collaborators;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "deviceOwner")
    private List<SessionEntity> sessions;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DeviceEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
