package com.egorgoncharov.krot.backend.model.relational.entity;

import com.egorgoncharov.krot.backend.model.Identifiable;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.*;
import lombok.*;

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
@Table(name = "device_collaborator")
public class DeviceCollaboratorEntity implements Identifiable<UUID> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_id")
    private DeviceEntity device;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private UserEntity collaborator;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "device_collaborator_programs", joinColumns = @JoinColumn(name = "collaborator"))
    @Column(name = "programs", nullable = false)
    private List<UUID> programs;
    @Column(name = "can_read_address", nullable = false)
    private Boolean canReadAddress;
    @Column(name = "can_read_password", nullable = false)
    private Boolean canReadPassword;
    @Column(name = "can_read_last_call", nullable = false)
    private Boolean canReadLastUpdate;
    @Column(name = "can_update_name", nullable = false)
    private Boolean canUpdateName;
    @Column(name = "can_update_password", nullable = false)
    private Boolean canUpdatePassword;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DeviceCollaboratorEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
