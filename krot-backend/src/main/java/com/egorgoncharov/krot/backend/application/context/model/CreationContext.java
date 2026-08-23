package com.egorgoncharov.krot.backend.application.context.model;

import com.egorgoncharov.krot.backend.application.context.AbstractApplicationContext;
import com.egorgoncharov.krot.backend.application.context.EventContextBuilder;
import com.egorgoncharov.krot.backend.application.events.EventType;
import com.egorgoncharov.krot.backend.database.Identifiable;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Getter
public class CreationContext<T extends Identifiable<I>, I, A> extends AbstractApplicationContext<A> implements EventContextBuilder<T, I, A> {
    private final T entity;

    @Override
    public EventContext<T, I, A> toEventContext() {
        return EventContext.<T, I, A>builder()
                .targets(List.of(entity))
                .event(EventType.CREATED)
                .principal(principal())
                .additionalContext(additionalContext())
                .build();
    }
}
