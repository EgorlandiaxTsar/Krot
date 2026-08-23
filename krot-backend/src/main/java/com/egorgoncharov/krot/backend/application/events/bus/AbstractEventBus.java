package com.egorgoncharov.krot.backend.application.events.bus;

import com.egorgoncharov.krot.backend.application.context.model.EventContext;
import com.egorgoncharov.krot.backend.database.Identifiable;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;

/* Events bus should be used only for app-local events stream,
 * if a bean must listen to all events (including other nodes),
 * it should create an AbstractDeliveryDispatcher
 * */
public abstract class AbstractEventBus<T extends Identifiable<I>, I, A> implements EventBus<T, I, A> {
    protected final BroadcastProcessor<EventContext<T, I, A>> processor = BroadcastProcessor.create();

    @Override
    public void publish(EventContext<T, I, A> data) {
        processor.onNext(data);
    }

    @Override
    public Multi<EventContext<T, I, A>> stream() {
        return processor;
    }
}
