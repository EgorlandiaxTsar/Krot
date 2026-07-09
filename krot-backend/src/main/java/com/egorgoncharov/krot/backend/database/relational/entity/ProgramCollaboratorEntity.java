package com.egorgoncharov.krot.backend.database.relational.entity;

import com.egorgoncharov.krot.backend.database.Identifiable;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@RegisterForReflection
@Entity
@Table(name = "program_collaborator")
public class ProgramCollaboratorEntity implements Identifiable<UUID> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "program_id")
    private ProgramEntity program;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private UserEntity collaborator;
    @Column(name = "can_update_name", nullable = false)
    private Boolean canUpdateName;
    @Column(name = "can_update_code", nullable = false)
    private Boolean canUpdateCode;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProgramCollaboratorEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
