package com.egorgoncharov.krot.backend.api.rest.core.session.response;

import com.egorgoncharov.krot.backend.api.rest.request.EntityReflection;
import com.egorgoncharov.krot.backend.database.relational.entity.DeviceEntity;
import com.egorgoncharov.krot.backend.database.relational.entity.HistoricalSessionEntity;
import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
import com.egorgoncharov.krot.backend.util.Types;
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
        return HistoricalSessionEntity.builder().id(Types.toUUID(id)).userOwner(isOwnerDevice ? null : UserEntity.builder().id(Types.toUUID(ownerId)).build()).deviceOwner(isOwnerDevice ? DeviceEntity.builder().id(Types.toUUID(ownerId)).build() : null).validUntil(OffsetDateTime.ofInstant(Instant.ofEpochMilli(validUntil), ZoneId.of("UTC"))).createdAt(OffsetDateTime.ofInstant(Instant.ofEpochMilli(createdAt), ZoneId.of("UTC"))).build();
    }

    @Override
    public EntityReflection<HistoricalSessionEntity> from(HistoricalSessionEntity o) {
        boolean isOwnerDevice = o.getUserOwner() == null;
        return new Session(o.getId().toString(), isOwnerDevice ? o.getDeviceOwner().getId().toString() : o.getUserOwner().getId().toString(), isOwnerDevice, o.getValidUntil().toInstant().toEpochMilli(), o.getCreatedAt().toInstant().toEpochMilli());
    }
}
