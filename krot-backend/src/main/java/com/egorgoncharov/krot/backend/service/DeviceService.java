package com.egorgoncharov.krot.backend.service;

import com.egorgoncharov.krot.backend.dto.service.Result;
import com.egorgoncharov.krot.backend.dto.service.filter.TimeRangeFilter;
import com.egorgoncharov.krot.backend.dto.service.pagination.Page;
import com.egorgoncharov.krot.backend.dto.service.pagination.PaginationOptions;
import com.egorgoncharov.krot.backend.model.entity.DeviceCollaboratorEntity;
import com.egorgoncharov.krot.backend.model.entity.DeviceEntity;
import com.egorgoncharov.krot.backend.service.common.CrudService;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.UUID;

public interface DeviceService extends CrudService<DeviceEntity, UUID> {
    Uni<Result<Page<DeviceEntity>>> filter(List<UUID> ids, TimeRangeFilter lastUpdateTime, TimeRangeFilter creationTime, UUID ownerId, String nameQuery, String addressQuery, PaginationOptions pagination);

    Uni<Result<DeviceCollaboratorEntity>> upsertCollaborator(DeviceCollaboratorEntity collaborator);

    Uni<Result<DeviceCollaboratorEntity>> deleteCollaborator(DeviceCollaboratorEntity collaborator);

    default Uni<Result<DeviceCollaboratorEntity>> deleteCollaborator(UUID id) {
        return deleteCollaborator(DeviceCollaboratorEntity.builder().id(id).build());
    }

    Uni<Result<Void>> transferOwnership(UUID deviceId, UUID newOwnerId);
}
