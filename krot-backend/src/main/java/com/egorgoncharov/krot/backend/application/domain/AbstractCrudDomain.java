package com.egorgoncharov.krot.backend.application.domain;

import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.application.context.AbstractApplicationContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityQueryContext;
import com.egorgoncharov.krot.backend.application.context.loader.CrudContextLoader;
import com.egorgoncharov.krot.backend.application.evaluator.CrudPermissionsEvaluator;
import com.egorgoncharov.krot.backend.application.exception.ApplicationException;
import com.egorgoncharov.krot.backend.application.query.AbstractApplicationQuery;
import com.egorgoncharov.krot.backend.application.query.pagination.Page;
import com.egorgoncharov.krot.backend.database.Identifiable;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

public abstract class AbstractCrudDomain<T extends Identifiable<I>, I, A, Q extends AbstractApplicationQuery<I>> extends AbstractServiceDomain<T, I, A> implements CrudDomain<T, I, Q> {
    protected abstract CrudContextLoader<T, I, A, Q> contextLoader();

    protected abstract CrudPermissionsEvaluator<T, I, A> permissionsEvaluator();

    @Override
    public Uni<Result<T>> create(T entity) {
        return executePipeline(
                contextLoader().creationContext(entity),
                context -> permissionsEvaluator().canCreate(context),
                context -> service().create(context)
        );
    }

    @Override
    public Uni<Result<Page<T>>> query(Q query) {
        return contextLoader().queryContext(query).flatMap(contextResult -> {
            if (contextResult.getCode() != 200 || contextResult.getResult().isEmpty()) return Uni.createFrom().failure(new ApplicationException(contextResult));
            EntityQueryContext<T, I, A> context = contextResult.getResult().get();
            return permissionsEvaluator().canView(context).chain(permissionsResult -> {
                if (permissionsResult.getResult().isEmpty()) return Uni.createFrom().failure(new ApplicationException(permissionsResult));
                List<T> items = context.getResults().getItems();
                List<Boolean> permissions = permissionsResult.getResult().get();
                List<T> filteredItems = IntStream.range(0, items.size()).mapToObj(i -> Boolean.TRUE.equals(permissions.get(i)) ? items.get(i) : null).toList();
                context.getResults().setItems(filteredItems);
                return Result.ok(context.getResults()).toUni();
            });
        });
    }

    @Override
    public Uni<Result<T>> update(T entity) {
        return executePipeline(
                contextLoader().updateContext(entity),
                context -> permissionsEvaluator().canUpdate(context),
                context -> service().update(context)
        );
    }

    @Override
    public Uni<Result<List<T>>> delete(List<T> entities) {
        return executePipeline(
                contextLoader().deleteContext(entities),
                context -> permissionsEvaluator().canDelete(context),
                context -> service().delete(context)
        );
    }

    protected <C extends AbstractApplicationContext<A>, R> Uni<Result<R>> executePipeline(Uni<Result<? extends C>> contextLoaderFn, Function<C, Uni<Result<Boolean>>> permissionsFn, Function<C, Uni<Result<R>>> serviceFn) {
        return contextLoaderFn.flatMap(contextResult -> {
            if (contextResult.getCode() != 200 || contextResult.getResult().isEmpty()) return Uni.createFrom().failure(new ApplicationException(contextResult));
            C context = contextResult.getResult().get();
            return permissionsFn.apply(context).chain(permissionsResult -> {
                if (permissionsResult.getResult().isEmpty() || !permissionsResult.getResult().get()) return Uni.createFrom().failure(new ApplicationException(permissionsResult));
                return Uni.createFrom().nullItem();
            }).flatMap(e -> serviceFn.apply(context)).flatMap(result -> {
                if (result.getCode() != 200 || result.getResult().isEmpty()) {
                    return Uni.createFrom().failure(new ApplicationException(result));
                }
                return result.toUni();
            });
        });
    }
}
