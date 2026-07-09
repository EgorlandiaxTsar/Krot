package com.egorgoncharov.krot.backend.application.context.common;

import com.egorgoncharov.krot.backend.application.context.AbstractApplicationContext;
import com.egorgoncharov.krot.backend.database.Identifiable;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class EntityUpdateContext<T extends Identifiable<I>, I, A> extends AbstractApplicationContext<A> {
    T oldEntity;
    T newEntity;
}
