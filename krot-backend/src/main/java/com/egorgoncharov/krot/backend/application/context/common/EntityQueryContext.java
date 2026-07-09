package com.egorgoncharov.krot.backend.application.context.common;

import com.egorgoncharov.krot.backend.application.context.AbstractApplicationContext;
import com.egorgoncharov.krot.backend.application.query.pagination.Page;
import com.egorgoncharov.krot.backend.database.Identifiable;
import lombok.Getter;
import lombok.experimental.SuperBuilder;


@SuperBuilder
@Getter
public class EntityQueryContext<T extends Identifiable<I>, I, A> extends AbstractApplicationContext<A> {
    private final Page<T> results;
}
