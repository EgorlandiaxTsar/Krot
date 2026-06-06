package com.egorgoncharov.krot.backend.service;

import com.egorgoncharov.krot.backend.dto.service.Result;
import com.egorgoncharov.krot.backend.dto.service.filter.TimeRangeFilter;
import com.egorgoncharov.krot.backend.dto.service.pagination.Page;
import com.egorgoncharov.krot.backend.dto.service.pagination.PaginationOptions;
import com.egorgoncharov.krot.backend.model.entity.SessionEntity;
import com.egorgoncharov.krot.backend.service.common.CrudService;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.UUID;

public interface AuthenticationService extends CrudService<SessionEntity, UUID> {
    Uni<Result<Page<SessionEntity>>> filter(List<UUID> ids, UUID userOwnerId, UUID deviceOwnerId, TimeRangeFilter lastUsedTime, TimeRangeFilter validUntilTime, TimeRangeFilter creationTime, String srcKeyQuery, String handshakeKeyQuery, String encryptionKeyQuery, PaginationOptions pagination);
}
