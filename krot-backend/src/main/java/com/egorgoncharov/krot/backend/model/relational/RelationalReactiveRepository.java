package com.egorgoncharov.krot.backend.model.relational;

import com.egorgoncharov.krot.backend.model.Identifiable;
import com.egorgoncharov.krot.backend.model.ReactiveRepository;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.transaction.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface RelationalReactiveRepository<T extends Identifiable<I>, I> extends ReactiveRepository<T, I>, PanacheRepositoryBase<T, I> {
    interface PropertyProvider<T, E> {
        E extract(T o);
    }

    default <E> Uni<T> findBy(E property, String field) {
        return findBy(Collections.singletonList(property), field).map(e -> e.isEmpty() ? null : e.getFirst());
    }

    default <E> Uni<List<T>> findBy(List<E> properties, String field) {
        if (properties == null || properties.isEmpty()) return Uni.createFrom().item(List.of());
        return find("1=1 AND " + field + " IN :values", Map.of("values", properties.stream().map(E::toString).toList())).list();
    }

    default <E> Uni<Boolean> existsBy(E property, String field, PropertyProvider<T, E> provider) {
        return existsBy(Collections.singletonList(property), field, provider).map(e -> e.isEmpty() ? null : e.getFirst());
    }

    default <E> Uni<List<Boolean>> existsBy(List<E> properties, String field, PropertyProvider<T, E> provider) {
        /* Since field is a fixed input passed programmatically by developer,
         * and Panache doesn't allow field names as variables, string concatenation
         * is used without SQL injection safeguards
         * */
        return findBy(properties, field).map(items -> items.stream().map(provider::extract).toList()).map(items -> properties.stream().map(items::contains).toList());
    }

    @Override
    default Uni<T> findById(I id) {
        return findBy(id, "id");
    }

    @Override
    default Uni<List<T>> findById(List<I> ids) {
        return findBy(ids, "id");
    }

    @Override
    default Uni<Boolean> existsById(I id) {
        return existsById(Collections.singletonList(id)).map(e -> e.isEmpty() ? null : e.getFirst());
    }

    @Override
    default Uni<List<Boolean>> existsById(List<I> ids) {
        return existsBy(ids, "id", (PropertyProvider<T, I>) Identifiable::getId);
    }

    @Override
    @Transactional
    default Uni<T> save(T entity) {
        if (isPersistent(entity)) return Uni.createFrom().item(entity);
        return getSession().chain(s -> s.merge(entity));
    }

    @Override
    @Transactional
    default Uni<List<T>> save(List<T> entities) {
        if (entities == null || entities.isEmpty()) return Uni.createFrom().item(List.of());
        return Uni.join().all(entities.stream().map(this::save).toList()).andFailFast();
    }

    @Override
    @Transactional
    default Uni<T> removeById(I id) {
        return findById(id).flatMap(e -> {
            if (e == null) return Uni.createFrom().nullItem();
            return delete(e).replaceWith(e);
        });
    }

    @Override
    @Transactional
    default Uni<List<T>> removeById(List<I> ids) {
        if (ids == null || ids.isEmpty()) return Uni.createFrom().item(List.of());
        return findById(ids).flatMap(items -> {
            if (items.isEmpty()) return Uni.createFrom().item(List.of());
            return delete("id IN ?1", ids).replaceWith(items);
        });
    }
}
