package com.egorgoncharov.krot.backend.application.context.model;

import com.egorgoncharov.krot.backend.application.query.pagination.Page;
import com.egorgoncharov.krot.backend.database.Identifiable;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;


@SuperBuilder
@Getter
public class QueryContext<T extends Identifiable<I>, I, A> extends ViewContext<T, I, A> {
    private final Page<T> results;

    @Override
    public List<T> targets() {
        return results.getItems();
    }
}
