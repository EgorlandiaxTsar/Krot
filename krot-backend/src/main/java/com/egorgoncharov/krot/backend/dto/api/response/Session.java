package com.egorgoncharov.krot.backend.dto.api.response;

import com.egorgoncharov.krot.backend.dto.api.request.EntityReflection;
import com.egorgoncharov.krot.backend.model.relational.entity.DeviceEntity;
import com.egorgoncharov.krot.backend.model.relational.entity.HistoricalSessionEntity;
import com.egorgoncharov.krot.backend.model.relational.entity.UserEntity;
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
public class Session implements EntityReflection<HistoricalSessionEntity> {
    @JsonProperty("id")
    private String id;
    @JsonProperty("ownerId")
    private String ownerId;
    @JsonProperty("isOwnerDevice")
    private boolean isOwnerDevice;
    @JsonProperty("validUntil")
    private long validUntil;
    @JsonProperty("createdAt")
    private long createdAt;

    @Override
    public HistoricalSessionEntity to() {
        return HistoricalSessionEntity.builder().id(TypesHelper.toUUID(id)).userOwner(isOwnerDevice ? null : UserEntity.builder().id(TypesHelper.toUUID(ownerId)).build()).deviceOwner(isOwnerDevice ? DeviceEntity.builder().id(TypesHelper.toUUID(ownerId)).build() : null).validUntil(OffsetDateTime.ofInstant(Instant.ofEpochMilli(validUntil), ZoneId.of("UTC"))).createdAt(OffsetDateTime.ofInstant(Instant.ofEpochMilli(createdAt), ZoneId.of("UTC"))).build();
    }

    @Override
    public EntityReflection<HistoricalSessionEntity> from(HistoricalSessionEntity o) {
        boolean isOwnerDevice = o.getUserOwner() == null;
        return new Session(o.getId().toString(), isOwnerDevice ? o.getDeviceOwner().getId().toString() : o.getUserOwner().getId().toString(), isOwnerDevice, o.getValidUntil().toInstant().toEpochMilli(), o.getCreatedAt().toInstant().toEpochMilli());
    }
}
