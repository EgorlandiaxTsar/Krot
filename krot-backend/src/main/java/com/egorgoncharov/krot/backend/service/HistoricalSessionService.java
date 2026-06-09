package com.egorgoncharov.krot.backend.service;

import com.egorgoncharov.krot.backend.dto.service.Result;
import com.egorgoncharov.krot.backend.dto.service.filter.TimeRangeFilter;
import com.egorgoncharov.krot.backend.dto.service.pagination.Page;
import com.egorgoncharov.krot.backend.dto.service.pagination.PaginationOptions;
import com.egorgoncharov.krot.backend.model.relational.entity.HistoricalSessionEntity;
import com.egorgoncharov.krot.backend.service.common.CrudService;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.UUID;

public interface HistoricalSessionService extends CrudService<HistoricalSessionEntity, UUID> {
    Uni<Result<Page<HistoricalSessionEntity>>> filter(List<UUID> ids, UUID userOwnerId, UUID deviceOwnerId, TimeRangeFilter validUntilTime, TimeRangeFilter creationTime, PaginationOptions pagination);
}
