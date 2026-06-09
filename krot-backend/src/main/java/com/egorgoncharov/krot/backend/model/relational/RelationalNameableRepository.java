package com.egorgoncharov.krot.backend.model.relational;

import com.egorgoncharov.krot.backend.model.Identifiable;
import com.egorgoncharov.krot.backend.model.Nameable;
import io.smallrye.mutiny.Uni;

import java.util.Collections;
import java.util.List;

public interface RelationalNameableRepository<T extends Identifiable<I> & Nameable, I> extends RelationalReactiveRepository<T, I> {
    default Uni<T> findByName(String name) {
        return findBy(name, sqlNameField());
    }

    default Uni<List<T>> findByName(List<String> names) {
        return findBy(names, sqlNameField());
    }

    default Uni<Boolean> existsByName(String name) {
        return existsByName(Collections.singletonList(name)).map(List::getFirst);
    }

    default Uni<List<Boolean>> existsByName(List<String> names) {
        return existsBy(names, sqlNameField(), (PropertyProvider<T, String>) T::getName);
    }

    default String sqlNameField() {
        return "name";
    }
}
