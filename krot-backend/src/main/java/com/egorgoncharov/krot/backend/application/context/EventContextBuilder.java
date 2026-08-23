package com.egorgoncharov.krot.backend.application.context;

import com.egorgoncharov.krot.backend.application.context.model.EventContext;
import com.egorgoncharov.krot.backend.database.Identifiable;

public interface EventContextBuilder<T extends Identifiable<I>, I, A> {
    EventContext<T, I, A> toEventContext();
}
