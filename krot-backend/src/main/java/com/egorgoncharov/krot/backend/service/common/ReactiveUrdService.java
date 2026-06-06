package com.egorgoncharov.krot.backend.service.common;

import com.egorgoncharov.krot.backend.dto.service.Result;
import com.egorgoncharov.krot.backend.model.common.Identifiable;
import io.smallrye.mutiny.Uni;

import java.util.function.Function;

/*
 * This is a URD service, which stands for upsert, read, delete. It can be used in
 * services that have the exact same pre-action logic in entity creation and update,
 * like same business logic checks. This interface provides one single abstract
 * method upsert, which is injected into basic CRUD service create and update
 * methods.
 * */
public abstract class ReactiveUrdService<T extends Identifiable<I>, I> extends ReactiveCrudService<T, I> {
    public abstract Uni<Result<T>> upsert(T o, Function<T, Uni<Result<T>>> action);

    @Override
    public Uni<Result<T>> create(T o) {
        return upsert(o, super::create);
    }

    @Override
    public Uni<Result<T>> update(T o) {
        return upsert(o, super::update);
    }
}
