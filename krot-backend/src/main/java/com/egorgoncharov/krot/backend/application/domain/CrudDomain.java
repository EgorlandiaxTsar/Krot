package com.egorgoncharov.krot.backend.application.domain;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.application.query.AbstractApplicationQuery;
import com.egorgoncharov.krot.backend.application.query.pagination.Page;
import com.egorgoncharov.krot.backend.database.Identifiable;
import com.egorgoncharov.krot.backend.util.Streams;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface CrudDomain<T extends Identifiable<I>, I, Q extends AbstractApplicationQuery<I>> {
    Uni<Result<T>> create(T entity);

    Uni<Result<Page<T>>> query(Q query);

    Uni<Result<T>> update(T entity);

    Uni<Result<List<T>>> delete(List<T> entities);

    default Uni<Result<T>> delete(T entity) {
        return Streams.singletonUni(delete(List.of(entity)));
    }
}
