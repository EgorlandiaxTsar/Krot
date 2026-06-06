package com.egorgoncharov.krot.backend.dto.api.response;

import com.egorgoncharov.krot.backend.dto.api.request.EntityReflection;
import com.egorgoncharov.krot.backend.model.entity.RoleEntity;
import com.egorgoncharov.krot.backend.model.entity.UserEntity;
import com.egorgoncharov.krot.backend.service.helper.TypesHelper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.ALWAYS)
public class User implements EntityReflection<UserEntity> {
    @JsonProperty("id")
    private String id;
    @JsonProperty("username")
    private String username;
    @JsonProperty("roleId")
    private String roleId;
    @JsonProperty("active")
    private boolean active;
    @JsonProperty("createdAt")
    private long createdAt;

    @Override
    public UserEntity to() {
        return UserEntity.builder().id(TypesHelper.toUUID(id)).username(username).role(RoleEntity.builder().id(TypesHelper.toUUID(roleId)).build()).active(active).createdAt(OffsetDateTime.ofInstant(Instant.ofEpochMilli(createdAt), ZoneId.of("UTC"))).build();
    }

    @Override
    public EntityReflection<UserEntity> from(UserEntity o) {
        return new User(o.getId().toString(), o.getUsername(), o.getRole().getId().toString(), o.getActive(), o.getCreatedAt().toInstant().toEpochMilli());
    }
}
