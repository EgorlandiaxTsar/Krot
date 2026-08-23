package com.egorgoncharov.krot.backend.api.model.response.program;

import com.egorgoncharov.krot.backend.api.model.request.EntityReflection;
import com.egorgoncharov.krot.backend.database.relational.entity.ProgramCollaboratorEntity;
import com.egorgoncharov.krot.backend.database.relational.entity.ProgramEntity;
import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
import com.egorgoncharov.krot.backend.util.Types;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ProgramCollaborator implements EntityReflection<ProgramCollaboratorEntity> {
    @JsonProperty("id")
    private String id;
    @JsonProperty("userId")
    private String userId;
    @JsonProperty("programId")
    private String programId;
    @JsonProperty("canUpdateName")
    private boolean canUpdateName;
    @JsonProperty("canUpdateCode")
    private boolean canUpdateCode;

    @Override
    public ProgramCollaboratorEntity to() {
        return ProgramCollaboratorEntity.builder().id(Types.toUUID(id)).program(ProgramEntity.builder().id(Types.toUUID(programId)).build()).collaborator(UserEntity.builder().id(Types.toUUID(userId)).build()).canUpdateName(canUpdateName).canUpdateCode(canUpdateCode).build();
    }

    @Override
    public EntityReflection<ProgramCollaboratorEntity> from(ProgramCollaboratorEntity o) {
        return new ProgramCollaborator(o.getId().toString(), o.getCollaborator().getId().toString(), o.getProgram().getId().toString(), o.getCanUpdateName(), o.getCanUpdateCode());
    }
}
