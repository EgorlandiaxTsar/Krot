package com.egorgoncharov.krot.backend.service;

import com.egorgoncharov.krot.backend.dto.service.Result;
import com.egorgoncharov.krot.backend.dto.service.filter.TimeRangeFilter;
import com.egorgoncharov.krot.backend.dto.service.pagination.Page;
import com.egorgoncharov.krot.backend.dto.service.pagination.PaginationOptions;
import com.egorgoncharov.krot.backend.model.entity.UserEntity;
import com.egorgoncharov.krot.backend.service.common.CrudService;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.UUID;

public interface UserService extends CrudService<UserEntity, UUID> {
    Uni<Result<Page<UserEntity>>> filter(List<UUID> ids, Boolean active, TimeRangeFilter creationTime, UUID roleId, String usernameQuery, PaginationOptions pagination);

    Uni<Result<Void>> updatePassword(UserEntity user, String newPassword);
}
