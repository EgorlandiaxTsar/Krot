package com.egorgoncharov.krot.backend.application.service;

import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.application.context.common.EntityCreationContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityDeleteContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityUpdateContext;
import com.egorgoncharov.krot.backend.database.Identifiable;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface ModifyingService<T extends Identifiable<I>, I, A> {
    Uni<Result<T>> create(EntityCreationContext<T, I, A> context);

    Uni<Result<T>> update(EntityUpdateContext<T, I, A> context);

    Uni<Result<List<T>>> delete(EntityDeleteContext<T, I, A> context);
}
