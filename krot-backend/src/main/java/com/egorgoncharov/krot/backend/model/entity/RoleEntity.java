package com.egorgoncharov.krot.backend.model.entity;

import com.egorgoncharov.krot.backend.model.common.Identifiable;
import com.egorgoncharov.krot.backend.model.common.Nameable;
import com.egorgoncharov.krot.backend.security.Authority;
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
@Table(name = "role")
public class RoleEntity implements Identifiable<UUID>, Nameable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "name", nullable = false, unique = true, length = 32)
    private String name;
    @Column(name = "grade", nullable = false)
    private Integer grade;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "role_authorities", joinColumns = @JoinColumn(name = "role"))
    @Column(name = "authority", nullable = false, length = 64)
    private List<Authority> authorities;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "role")
    private List<UserEntity> users;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RoleEntity that)) return false;
        return Objects.equals(grade, that.grade) && Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, grade);
    }
}
