package com.egorgoncharov.krot.backend.dto.api.response;

import com.egorgoncharov.krot.backend.dto.api.request.EntityReflection;
import com.egorgoncharov.krot.backend.model.relational.entity.DeviceCollaboratorEntity;
import com.egorgoncharov.krot.backend.model.relational.entity.DeviceEntity;
import com.egorgoncharov.krot.backend.model.relational.entity.UserEntity;
import com.egorgoncharov.krot.backend.service.helper.TypesHelper;
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
public class Device implements EntityReflection<DeviceEntity> {
    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("address")
    private String address;
    @JsonProperty("password")
    private String password;
    @JsonProperty("ownerId")
    private String ownerId;
    @JsonProperty("lastCalled")
    private long lastCalled;
    @JsonProperty("createdAt")
    private long createdAt;
    @JsonProperty("collaborators")
    private List<DeviceCollaborator> collaborators;

    @Override
    public DeviceEntity to() {
        return DeviceEntity.builder().id(TypesHelper.toUUID(id)).name(name).address(address).password(password).owner(UserEntity.builder().id(TypesHelper.toUUID(ownerId)).build()).lastCalled(OffsetDateTime.ofInstant(Instant.ofEpochMilli(lastCalled), ZoneId.of("UTC"))).createdAt(OffsetDateTime.ofInstant(Instant.ofEpochMilli(createdAt), ZoneId.of("UTC"))).collaborators(collaborators.stream().map(collaborator -> DeviceCollaboratorEntity.builder().id(TypesHelper.toUUID(collaborator.getId())).build()).toList()).build();
    }

    @Override
    public EntityReflection<DeviceEntity> from(DeviceEntity o) {
        return new Device(o.getId().toString(), o.getName(), o.getAddress(), o.getPassword(), o.getOwner().getId().toString(), o.getLastCalled().toInstant().toEpochMilli(), o.getCreatedAt().toInstant().toEpochMilli(), o.getCollaborators().stream().map(collaborator -> (DeviceCollaborator) (new DeviceCollaborator().from(collaborator))).toList());
    }
}
