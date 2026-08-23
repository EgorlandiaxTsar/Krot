package com.egorgoncharov.krot.backend.application.events.delivery.dispatcher;

import com.egorgoncharov.krot.backend.application.events.delivery.AbstractDeliveryManager;
import com.egorgoncharov.krot.backend.database.Identifiable;

public abstract class AbstractDeliveryDispatcher<T extends Identifiable<I>, I, A> implements DeliveryDispatcher<T, I, A> {
    protected abstract AbstractDeliveryManager<T, I, A> deliveryManager();

    protected void start() {
        deliveryManager().registerDispatcher(this);
    }
}
