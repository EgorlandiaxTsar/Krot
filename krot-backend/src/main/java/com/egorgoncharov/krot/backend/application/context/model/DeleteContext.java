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
public class DeleteContext<T extends Identifiable<I>, I, A> extends AbstractApplicationContext<A> implements EventContextBuilder<T, I, A> {
    private final List<T> entities;

    @Override
    public EventContext<T, I, A> toEventContext() {
        return EventContext.<T, I, A>builder()
                .targets(entities)
                .event(EventType.DELETED)
                .principal(principal())
                .additionalContext(additionalContext())
                .build();
    }
}
