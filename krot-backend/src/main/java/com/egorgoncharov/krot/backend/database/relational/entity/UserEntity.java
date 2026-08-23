package com.egorgoncharov.krot.backend.database.relational.entity;

import com.egorgoncharov.krot.backend.database.Identifiable;
import com.egorgoncharov.krot.backend.database.Nameable;
import com.egorgoncharov.krot.backend.security.Authority;
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
@Entity
@Table(name = "`user`")
public class UserEntity implements Identifiable<UUID>, Nameable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "username", nullable = false, unique = true, length = 32)
    private String username;
    @Column(name = "password", nullable = false, length = 32)
    private String password;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private RoleEntity role;
    @Column(name = "active", nullable = false)
    private Boolean active;
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime createdAt;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "owner")
    private List<DeviceEntity> devices;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "owner")
    private List<ProgramEntity> programs;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "userOwner")
    private List<HistoricalSessionEntity> sessions;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "collaborator")
    private List<DeviceCollaboratorEntity> deviceCollaborations;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "collaborator")
    private List<ProgramCollaboratorEntity> programCollaborations;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserEntity that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(username, that.username) && Objects.equals(password, that.password) && Objects.equals(role.getId(), that.role.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, password, role.getId());
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public void setName(String name) {
        this.setUsername(name);
    }

    public boolean hasAuthority(Authority authority) {
        return getRole().getAuthorities().contains(authority);
    }
}
