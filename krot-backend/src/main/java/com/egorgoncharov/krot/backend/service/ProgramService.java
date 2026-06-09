package com.egorgoncharov.krot.backend.service;

import com.egorgoncharov.krot.backend.dto.service.Result;
import com.egorgoncharov.krot.backend.dto.service.filter.TimeRangeFilter;
import com.egorgoncharov.krot.backend.dto.service.pagination.Page;
import com.egorgoncharov.krot.backend.dto.service.pagination.PaginationOptions;
import com.egorgoncharov.krot.backend.model.relational.entity.ProgramEntity;
import com.egorgoncharov.krot.backend.service.common.CrudService;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.UUID;

public interface ProgramService extends CrudService<ProgramEntity, UUID> {
    Uni<Result<Page<ProgramEntity>>> filter(List<UUID> ids, TimeRangeFilter creationTime, UUID ownerId, String nameQuery, PaginationOptions pagination);

    Uni<Result<Void>> transferOwnership(UUID programId, UUID newOwnerId);
}
