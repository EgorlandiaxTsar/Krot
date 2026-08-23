package com.egorgoncharov.krot.backend.application.events.bus;

import com.egorgoncharov.krot.backend.application.context.model.EventContext;
import com.egorgoncharov.krot.backend.database.Identifiable;
import io.smallrye.mutiny.Multi;

public interface EventBus<T extends Identifiable<I>, I, A> {
    void publish(EventContext<T, I, A> data);

    Multi<EventContext<T, I, A>> stream();
}
