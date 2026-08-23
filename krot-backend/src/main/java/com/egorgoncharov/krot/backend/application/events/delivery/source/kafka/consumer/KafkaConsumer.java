package com.egorgoncharov.krot.backend.application.events.delivery.source.kafka.consumer;

import com.egorgoncharov.krot.backend.application.context.model.EventContext;
import com.egorgoncharov.krot.backend.application.events.bus.AbstractEventBus;
import com.egorgoncharov.krot.backend.application.events.delivery.AbstractDeliveryManager;
import com.egorgoncharov.krot.backend.application.events.delivery.source.kafka.KafkaEvent;
import com.egorgoncharov.krot.backend.application.exception.EventRecompositionException;
import com.egorgoncharov.krot.backend.database.Identifiable;
import com.egorgoncharov.krot.backend.database.relational.RelationalCrudRepository;
import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
import com.egorgoncharov.krot.backend.database.relational.repository.UserRepository;
import io.smallrye.mutiny.Uni;

public abstract class KafkaConsumer<T extends Identifiable<I>, I, A> extends AbstractEventBus<T, I, A> {
    protected abstract AbstractDeliveryManager<T, I, A> deliveryManager();

    protected abstract RelationalCrudRepository<T, I> repository();

    protected abstract UserRepository principalRepository();

    protected Uni<Void> consume(KafkaEvent<I> event) {
        return streamContext(event)
                .invoke(this::publish)
                .replaceWithVoid();
    }

    protected Uni<EventContext<T, I, A>> streamContext(KafkaEvent<I> event) {
        /* Although manual context loading is discouraged, in this case we are loading context in the late stage of request lifecycle,
         * while the context loader is meant to be used in its first stage.
         * */
        return principalRepository()
                .findById(event.getExecutorId())
                .flatMap(principal -> {
                    if (principal == null) return Uni.createFrom().failure(new EventRecompositionException("Failed to fetch executor, it was probably deleted in the moment of event propagation"));
                    return Uni.createFrom().item(principal);
                })
                .flatMap(principal -> repository()
                        .findById(event.getAffectedIds())
                        .map(items -> EventContext.<T, I, A>builder()
                                .targets(items)
                                .event(event.getEventType())
                                .principal(UserEntity.builder().id(event.getExecutorId()).build())
                                .additionalContext(null)
                                .build()
                        )
                );
    }

    protected void start() {
        deliveryManager().registerSource(this);
    }
}
