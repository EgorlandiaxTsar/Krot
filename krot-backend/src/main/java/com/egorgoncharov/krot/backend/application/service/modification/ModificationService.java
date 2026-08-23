package com.egorgoncharov.krot.backend.application.service.modification;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.application.context.model.CreationContext;
import com.egorgoncharov.krot.backend.application.context.model.DeleteContext;
import com.egorgoncharov.krot.backend.application.context.model.UpdateContext;
import com.egorgoncharov.krot.backend.database.Identifiable;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface ModificationService<T extends Identifiable<I>, I, A> {
    Uni<Result<T>> create(CreationContext<T, I, A> context);

    Uni<Result<T>> update(UpdateContext<T, I, A> context);

    Uni<Result<List<T>>> delete(DeleteContext<T, I, A> context);
}
