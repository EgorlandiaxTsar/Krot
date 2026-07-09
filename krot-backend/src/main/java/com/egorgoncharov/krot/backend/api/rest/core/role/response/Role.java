package com.egorgoncharov.krot.backend.api.rest.core.role.response;

import com.egorgoncharov.krot.backend.api.rest.request.EntityReflection;
import com.egorgoncharov.krot.backend.database.relational.entity.RoleEntity;
import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
import com.egorgoncharov.krot.backend.security.Authority;
import com.egorgoncharov.krot.backend.util.Types;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Role implements EntityReflection<RoleEntity> {
    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("grade")
    private int grade;
    @JsonProperty("authorities")
    private List<String> authorities;
    @JsonProperty("users")
    private List<String> users;

    @Override
    public RoleEntity to() {
        return RoleEntity.builder().id(Types.toUUID(id)).name(name).grade(grade).authorities(Types.toAuthoritiesList(authorities)).users(users.stream().map(user -> UserEntity.builder().id(Types.toUUID(user)).build()).toList()).build();
    }

    @Override
    public EntityReflection<RoleEntity> from(RoleEntity o) {
        return new Role(o.getId().toString(), o.getName(), o.getGrade(), o.getAuthorities().stream().map(Authority::name).toList(), o.getUsers().stream().map(user -> user.getId().toString()).toList());
    }
}
