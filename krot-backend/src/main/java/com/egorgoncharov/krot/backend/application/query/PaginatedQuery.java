package com.egorgoncharov.krot.backend.application.query;

import com.egorgoncharov.krot.backend.application.query.pagination.PaginationOptions;

public interface PaginatedQuery {
    PaginationOptions getPagination();
}
