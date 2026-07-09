package com.egorgoncharov.krot.backend.application.query;

import com.egorgoncharov.krot.backend.application.query.pagination.PaginationOptions;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Getter
public abstract class AbstractApplicationQuery<T> implements IdQuery<T>, PaginatedQuery {
    private final List<T> ids;
    private final PaginationOptions pagination;
}
