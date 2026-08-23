package com.egorgoncharov.krot.backend.application.context.loader;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.application.context.model.CreationContext;
import com.egorgoncharov.krot.backend.application.context.model.DeleteContext;
import com.egorgoncharov.krot.backend.application.context.model.QueryContext;
import com.egorgoncharov.krot.backend.application.context.model.UpdateContext;
import com.egorgoncharov.krot.backend.application.query.AbstractApplicationQuery;
import com.egorgoncharov.krot.backend.database.Identifiable;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface CrudContextLoader<T extends Identifiable<I>, I, A, Q extends AbstractApplicationQuery<I>> {
    Uni<Result<? extends CreationContext<T, I, A>>> creationContext(T entity, A subcontext);

    default Uni<Result<? extends CreationContext<T, I, A>>> creationContext(T entity) {
        return creationContext(entity, null);
    }

    Uni<Result<? extends QueryContext<T, I, A>>> queryContext(Q query, A subcontext);

    default Uni<Result<? extends QueryContext<T, I, A>>> queryContext(Q query) {
        return queryContext(query, null);
    }

    Uni<Result<? extends UpdateContext<T, I, A>>> updateContext(T entity, A subcontext);

    default Uni<Result<? extends UpdateContext<T, I, A>>> updateContext(T entity) {
        return updateContext(entity, null);
    }

    Uni<Result<? extends DeleteContext<T, I, A>>> deleteContext(List<T> entities, A subcontext);

    default Uni<Result<? extends DeleteContext<T, I, A>>> deleteContext(List<T> entities) {
        return deleteContext(entities, null);
    }
}
