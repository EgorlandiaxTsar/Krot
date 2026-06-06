package com.egorgoncharov.krot.backend.model.entity;

import com.egorgoncharov.krot.backend.model.common.Identifiable;
import com.egorgoncharov.krot.backend.model.common.Nameable;
import io.quarkus.runtime.annotations.RegisterForReflection;
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
@RegisterForReflection
@Entity
@Table(name = "program")
public class ProgramEntity implements Identifiable<UUID>, Nameable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "name", nullable = false, unique = true, length = 64)
    private String name;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    private UserEntity owner;
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProgramEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
