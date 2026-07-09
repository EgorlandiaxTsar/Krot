package com.egorgoncharov.krot.backend.application.evaluator;

import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.application.context.common.EntityCreationContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityDeleteContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityQueryContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityUpdateContext;
import com.egorgoncharov.krot.backend.database.Identifiable;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface CrudPermissionsEvaluator<T extends Identifiable<I>, I, A> {
    Uni<Result<Boolean>> canCreate(EntityCreationContext<T, I, A> context);

    Uni<Result<List<Boolean>>> canView(EntityQueryContext<T, I, A> context);

    Uni<Result<Boolean>> canUpdate(EntityUpdateContext<T, I, A> context);

    Uni<Result<Boolean>> canDelete(EntityDeleteContext<T, I, A> context);
}
