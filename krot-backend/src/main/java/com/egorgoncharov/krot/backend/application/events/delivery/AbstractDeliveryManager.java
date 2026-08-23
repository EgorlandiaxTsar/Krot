package com.egorgoncharov.krot.backend.application.events.delivery;

import com.egorgoncharov.krot.backend.application.context.model.EventContext;
import com.egorgoncharov.krot.backend.application.events.bus.AbstractEventBus;
import com.egorgoncharov.krot.backend.application.events.delivery.dispatcher.DeliveryDispatcher;
import com.egorgoncharov.krot.backend.database.Identifiable;
import io.smallrye.mutiny.subscription.Cancellable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractDeliveryManager<T extends Identifiable<I>, I, A> implements DeliveryManager<T, I, A> {
    private final Map<UUID, Cancellable> sources = new ConcurrentHashMap<>();
    private final Map<UUID, DeliveryDispatcher<T, I, A>> dispatchers = new ConcurrentHashMap<>();

    @Override
    public void deliver(EventContext<T, I, A> context) {
        dispatchers.forEach((id, dispatcher) -> dispatcher.dispatch(context));
    }

    @Override
    public UUID registerDispatcher(DeliveryDispatcher<T, I, A> dispatcher) {
        UUID id = UUID.randomUUID();
        dispatchers.put(id, dispatcher);
        return id;
    }

    @Override
    public void removeDispatcher(UUID id) {
        dispatchers.remove(id);
    }

    @Override
    public UUID registerSource(AbstractEventBus<T, I, A> source) {
        UUID id = UUID.randomUUID();
        Cancellable stream = source.stream().subscribe().with(this::deliver);
        sources.put(id, stream);
        return id;
    }

    @Override
    public void removeSource(UUID id) {
        sources.get(id).cancel();
        sources.remove(id);
    }
}
