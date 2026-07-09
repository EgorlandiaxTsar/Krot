package com.egorgoncharov.krot.backend.api.rest.core.device.response;

import com.egorgoncharov.krot.backend.api.rest.request.EntityReflection;
import com.egorgoncharov.krot.backend.database.relational.entity.DeviceCollaboratorEntity;
import com.egorgoncharov.krot.backend.database.relational.entity.DeviceEntity;
import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
import com.egorgoncharov.krot.backend.util.Types;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.ALWAYS)
public class DeviceCollaborator implements EntityReflection<DeviceCollaboratorEntity> {
    @JsonProperty("id")
    private String id;
    @JsonProperty("userId")
    private String userId;
    @JsonProperty("deviceId")
    private String deviceId;
    @JsonProperty("canReadAddress")
    private boolean canReadAddress;
    @JsonProperty("canReadPassword")
    private boolean canReadPassword;
    @JsonProperty("canReadLastUpdate")
    private boolean canReadLastUpdate;
    @JsonProperty("canUpdateName")
    private boolean canUpdateName;
    @JsonProperty("canUpdatePassword")
    private boolean canUpdatePassword;
    @JsonProperty("allowPrograms")
    private List<String> allowPrograms;

    @Override
    public DeviceCollaboratorEntity to() {
        return DeviceCollaboratorEntity.builder().id(Types.toUUID(id)).device(DeviceEntity.builder().id(Types.toUUID(deviceId)).build()).collaborator(UserEntity.builder().id(Types.toUUID(userId)).build()).programs(Types.toUUID(allowPrograms)).canReadAddress(canReadAddress).canReadPassword(canReadPassword).canReadLastUpdate(canReadLastUpdate).canUpdateName(canUpdateName).canUpdatePassword(canUpdatePassword).build();
    }

    @Override
    public EntityReflection<DeviceCollaboratorEntity> from(DeviceCollaboratorEntity o) {
        return new DeviceCollaborator(o.getId().toString(), o.getCollaborator().getId().toString(), o.getDevice().getId().toString(), o.getCanReadAddress(), o.getCanReadPassword(), o.getCanReadLastUpdate(), o.getCanUpdateName(), o.getCanUpdatePassword(), o.getPrograms().stream().map(UUID::toString).toList());
    }
}
