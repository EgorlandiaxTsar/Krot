package com.egorgoncharov.krot.backend.service;

import com.egorgoncharov.krot.backend.dto.service.Result;
import com.egorgoncharov.krot.backend.dto.service.filter.NumericalRangeFilter;
import com.egorgoncharov.krot.backend.dto.service.pagination.Page;
import com.egorgoncharov.krot.backend.dto.service.pagination.PaginationOptions;
import com.egorgoncharov.krot.backend.model.relational.entity.RoleEntity;
import com.egorgoncharov.krot.backend.model.relational.entity.UserEntity;
import com.egorgoncharov.krot.backend.service.common.CrudService;
import io.smallrye.mutiny.Uni;

import java.util.*;

public interface RoleService extends CrudService<RoleEntity, UUID> {
    Uni<Result<Page<RoleEntity>>> filter(List<UUID> ids, List<String> authorities, String nameQuery, NumericalRangeFilter grade, PaginationOptions pagination);

    Uni<Result<Map<RoleEntity, List<UserEntity>>>> findAssignedUsers(List<UUID> ids);

    default Uni<Result<List<UserEntity>>> findAssignedUsers(UUID id) {
        return findAssignedUsers(Collections.singletonList(id)).map(e -> new Result<>(Optional.ofNullable(e.getResult().isPresent() ? e.getResult().get().values().stream().findAny().orElse(null) : null), e.getMessage(), e.getCode(), e.getTimestamp()));
    }
}
