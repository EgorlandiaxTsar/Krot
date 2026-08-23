package com.egorgoncharov.krot.backend.application.events.delivery.source.kafka;

import com.egorgoncharov.krot.backend.application.context.model.EventContext;
import com.egorgoncharov.krot.backend.application.events.EventType;
import com.egorgoncharov.krot.backend.database.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KafkaEvent<I> {
    private UUID executorId;
    private List<I> affectedIds;
    private EventType eventType;

    public static <T extends Identifiable<I>, I, A> KafkaEvent<I> from(EventContext<T, I, A> context) {
        return new KafkaEvent<>(context.principal().getId(), context.targets().stream().map(T::getId).toList(), context.getEvent());
    }
}
