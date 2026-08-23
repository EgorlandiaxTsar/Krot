package com.egorgoncharov.krot.backend.application.context.model;

import com.egorgoncharov.krot.backend.application.events.EventType;
import com.egorgoncharov.krot.backend.database.Identifiable;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
public class EventContext<T extends Identifiable<I>, I, A> extends ViewContext<T, I, A> {
    private final List<T> targets;
    @Getter
    private final EventType event;

    @Override
    public List<T> targets() {
        return targets;
    }
}
