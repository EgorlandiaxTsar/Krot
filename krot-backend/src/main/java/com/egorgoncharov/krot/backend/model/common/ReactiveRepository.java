package com.egorgoncharov.krot.backend.model.common;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.transaction.Transactional;

import java.util.Collections;
import java.util.List;

public interface ReactiveRepository<T extends Identifiable<I>, I> extends PanacheRepositoryBase<T, I> {
    interface PropertyProvider<T, E> {
        E extract(T o);
    }

    default <E> Uni<T> findBy(E property, String field) {
        return findBy(Collections.singletonList(property), field).map(e -> e.isEmpty() ? null : e.getFirst());
    }

    default <E> Uni<List<T>> findBy(List<E> properties, String field) {
        if (properties == null || properties.isEmpty()) return Uni.createFrom().item(List.of());
        return find("1=1 AND " + field + " IN :values", Parameters.with("values", properties.stream().map(E::toString).toList()).map()).list();
    }

    default Uni<List<T>> findById(List<I> ids) {
        return findBy(ids, "id");
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

    default Uni<Boolean> existsById(I id) {
        return existsById(Collections.singletonList(id)).map(e -> e.isEmpty() ? null : e.getFirst());
    }

    default Uni<List<Boolean>> existsById(List<I> ids) {
        return existsBy(ids, "id", (PropertyProvider<T, I>) Identifiable::getId);
    }

    @Transactional
    default Uni<T> save(T entity) {
        if (isPersistent(entity)) return Uni.createFrom().item(entity);
        return getSession().chain(s -> s.merge(entity));
    }

    @Transactional
    default Uni<List<T>> save(List<T> entities) {
        if (entities == null || entities.isEmpty()) return Uni.createFrom().item(List.of());
        return Uni.join().all(entities.stream().map(this::save).toList()).andFailFast();
    }

    @Transactional
    default Uni<T> removeById(I id) {
        return findById(id).flatMap(e -> {
            if (e == null) return Uni.createFrom().nullItem();
            return delete(e).replaceWith(e);
        });
    }

    @Transactional
    default Uni<List<T>> removeById(List<I> ids) {
        if (ids == null || ids.isEmpty()) return Uni.createFrom().item(List.of());
        return findById(ids).flatMap(items -> {
            if (items.isEmpty()) return Uni.createFrom().item(List.of());
            return delete("id IN ?1", ids).replaceWith(items);
        });
    }
}
