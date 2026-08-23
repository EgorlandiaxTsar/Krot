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
public class UpdateContext<T extends Identifiable<I>, I, A> extends AbstractApplicationContext<A> implements EventContextBuilder<T, I, A> {
    T oldEntity;
    T newEntity;

    @Override
    public EventContext<T, I, A> toEventContext() {
        return EventContext.<T, I, A>builder()
                .targets(List.of(newEntity))
                .event(EventType.UPDATED)
                .principal(principal())
                .additionalContext(additionalContext())
                .build();
    }
}
