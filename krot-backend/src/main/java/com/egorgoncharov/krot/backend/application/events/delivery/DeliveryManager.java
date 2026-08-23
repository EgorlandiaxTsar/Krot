package com.egorgoncharov.krot.backend.application.events.delivery;

import com.egorgoncharov.krot.backend.application.context.model.EventContext;
import com.egorgoncharov.krot.backend.application.events.bus.AbstractEventBus;
import com.egorgoncharov.krot.backend.application.events.delivery.dispatcher.DeliveryDispatcher;
import com.egorgoncharov.krot.backend.database.Identifiable;

import java.util.UUID;

public interface DeliveryManager<T extends Identifiable<I>, I, A> {
    void deliver(EventContext<T, I, A> context);

    UUID registerDispatcher(DeliveryDispatcher<T, I, A> dispatcher);

    void removeDispatcher(UUID id);

    UUID registerSource(AbstractEventBus<T, I, A> source);

    void removeSource(UUID id);
}
