package com.egorgoncharov.krot.backend.application.context.loader;

import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.application.context.common.EntityCreationContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityDeleteContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityQueryContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityUpdateContext;
import com.egorgoncharov.krot.backend.application.query.AbstractApplicationQuery;
import com.egorgoncharov.krot.backend.database.Identifiable;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface CrudContextLoader<T extends Identifiable<I>, I, A, Q extends AbstractApplicationQuery<I>> {
    Uni<Result<? extends EntityCreationContext<T, I, A>>> creationContext(T entity, A subcontext);

    default Uni<Result<? extends EntityCreationContext<T, I, A>>> creationContext(T entity) {
        return creationContext(entity, null);
    }

    Uni<Result<? extends EntityQueryContext<T, I, A>>> queryContext(Q query, A subcontext);

    default Uni<Result<? extends EntityQueryContext<T, I, A>>> queryContext(Q query) {
        return queryContext(query, null);
    }

    Uni<Result<? extends EntityUpdateContext<T, I, A>>> updateContext(T entity, A subcontext);

    default Uni<Result<? extends EntityUpdateContext<T, I, A>>> updateContext(T entity) {
        return updateContext(entity, null);
    }

    Uni<Result<? extends EntityDeleteContext<T, I, A>>> deleteContext(List<T> entities, A subcontext);

    default Uni<Result<? extends EntityDeleteContext<T, I, A>>> deleteContext(List<T> entities) {
        return deleteContext(entities, null);
    }
}
