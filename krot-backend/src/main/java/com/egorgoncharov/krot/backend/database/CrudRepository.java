package com.egorgoncharov.krot.backend.database;

import io.smallrye.mutiny.Uni;

import java.util.List;

public interface CrudRepository<T extends Identifiable<I>, I> {
    Uni<T> findById(I id);

    Uni<List<T>> findById(List<I> ids);

    Uni<Boolean> existsById(I id);

    Uni<List<Boolean>> existsById(List<I> ids);

    Uni<T> save(T entity);

    Uni<List<T>> save(List<T> entities);

    Uni<T> removeById(I id);

    Uni<List<T>> removeById(List<I> ids);
}
