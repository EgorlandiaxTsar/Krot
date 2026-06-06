package com.egorgoncharov.krot.backend.dto.api.response;

import com.egorgoncharov.krot.backend.dto.api.request.EntityReflection;
import com.egorgoncharov.krot.backend.model.entity.ProgramEntity;
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
public class Program implements EntityReflection<ProgramEntity> {
    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("ownerId")
    private String ownerId;
    @JsonProperty("createAt")
    private long createdAt;

    @Override
    public ProgramEntity to() {
        return ProgramEntity.builder().id(TypesHelper.toUUID(id)).name(name).owner(UserEntity.builder().id(TypesHelper.toUUID(ownerId)).build()).createdAt(OffsetDateTime.ofInstant(Instant.ofEpochMilli(createdAt), ZoneId.of("UTC"))).build();
    }

    @Override
    public EntityReflection<ProgramEntity> from(ProgramEntity o) {
        return new Program(o.getId().toString(), o.getName(), o.getOwner().getId().toString(), o.getCreatedAt().toInstant().toEpochMilli());
    }
}
