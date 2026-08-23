package com.egorgoncharov.krot.backend.application.events.delivery.dispatcher;

import com.egorgoncharov.krot.backend.application.context.model.EventContext;
import com.egorgoncharov.krot.backend.database.Identifiable;

public interface DeliveryDispatcher<T extends Identifiable<I>, I, A> {
    void dispatch(EventContext<T, I, A> context);
}
