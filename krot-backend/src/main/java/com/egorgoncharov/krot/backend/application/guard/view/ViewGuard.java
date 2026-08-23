package com.egorgoncharov.krot.backend.application.guard.view;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.application.context.model.ViewContext;
import com.egorgoncharov.krot.backend.database.Identifiable;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface ViewGuard<T extends Identifiable<I>, I, A> {
    Uni<Result<List<Boolean>>> canView(ViewContext<T, I, A> context);
}
