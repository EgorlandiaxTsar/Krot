package com.egorgoncharov.krot.backend.application.guard.modification;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.application.context.model.CreationContext;
import com.egorgoncharov.krot.backend.application.context.model.DeleteContext;
import com.egorgoncharov.krot.backend.application.context.model.UpdateContext;
import com.egorgoncharov.krot.backend.database.Identifiable;
import io.smallrye.mutiny.Uni;

public interface ModificationGuard<T extends Identifiable<I>, I, A> {
    Uni<Result<Boolean>> canCreate(CreationContext<T, I, A> context);

    Uni<Result<Boolean>> canUpdate(UpdateContext<T, I, A> context);

    Uni<Result<Boolean>> canDelete(DeleteContext<T, I, A> context);
}
