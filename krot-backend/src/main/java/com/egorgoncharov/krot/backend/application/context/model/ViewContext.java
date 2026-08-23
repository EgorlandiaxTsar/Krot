package com.egorgoncharov.krot.backend.application.context.model;

import com.egorgoncharov.krot.backend.application.context.AbstractApplicationContext;
import com.egorgoncharov.krot.backend.database.Identifiable;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Getter
public abstract class ViewContext<T extends Identifiable<I>, I, A> extends AbstractApplicationContext<A> {
    public abstract List<T> targets();
}
