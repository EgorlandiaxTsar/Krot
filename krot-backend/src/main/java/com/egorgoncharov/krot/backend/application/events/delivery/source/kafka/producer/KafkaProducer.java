package com.egorgoncharov.krot.backend.application.events.delivery.source.kafka.producer;

import com.egorgoncharov.krot.backend.application.events.bus.AbstractEventBus;
import com.egorgoncharov.krot.backend.application.events.delivery.source.kafka.KafkaEvent;
import com.egorgoncharov.krot.backend.database.Identifiable;
import org.eclipse.microprofile.reactive.messaging.Emitter;

public abstract class KafkaProducer<T extends Identifiable<I>, I, A> {
    protected abstract AbstractEventBus<T, I, A> events();

    protected abstract Emitter<KafkaEvent<I>> emitter();

    protected void start() {
        events().stream().subscribe().with(context -> emitter().send(KafkaEvent.from(context)));
    }
}
