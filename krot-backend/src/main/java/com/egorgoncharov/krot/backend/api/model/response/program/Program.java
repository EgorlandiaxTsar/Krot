package com.egorgoncharov.krot.backend.api.model.response.program;

import com.egorgoncharov.krot.backend.api.model.request.EntityReflection;
import com.egorgoncharov.krot.backend.database.relational.entity.ProgramCollaboratorEntity;
import com.egorgoncharov.krot.backend.database.relational.entity.ProgramEntity;
import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
import com.egorgoncharov.krot.backend.util.Types;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

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
    @JsonProperty("collaborators")
    private List<ProgramCollaborator> collaborators;

    @Override
    public ProgramEntity to() {
        return ProgramEntity.builder().id(Types.toUUID(id)).name(name).owner(UserEntity.builder().id(Types.toUUID(ownerId)).build()).createdAt(OffsetDateTime.ofInstant(Instant.ofEpochMilli(createdAt), ZoneId.of("UTC"))).collaborators(collaborators.stream().map(collaborator -> ProgramCollaboratorEntity.builder().id(Types.toUUID(collaborator.getId())).build()).toList()).build();
    }

    @Override
    public EntityReflection<ProgramEntity> from(ProgramEntity o) {
        return new Program(o.getId().toString(), o.getName(), o.getOwner().getId().toString(), o.getCreatedAt().toInstant().toEpochMilli(), o.getCollaborators().stream().map(collaborator -> (ProgramCollaborator) (new ProgramCollaborator().from(collaborator))).toList());
    }
}
