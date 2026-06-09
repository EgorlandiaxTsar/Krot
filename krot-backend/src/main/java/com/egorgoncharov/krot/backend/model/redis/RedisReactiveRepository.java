package com.egorgoncharov.krot.backend.model.redis;

import com.egorgoncharov.krot.backend.model.Identifiable;
import com.egorgoncharov.krot.backend.model.ReactiveRepository;
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.hash.ReactiveHashCommands;
import io.smallrye.mutiny.Uni;

import java.time.Duration;
import java.util.List;

public interface RedisReactiveRepository<T extends Identifiable<I>, I> extends ReactiveRepository<T, I> {
    String getRedisNamespace();

    ReactiveHashCommands<String, String, T> getHashCommands();

    ReactiveRedisDataSource getRedisDataSource();

    default String buildEntityKey(I id) {
        return getRedisNamespace() + ":" + id.toString();
    }

    @Override
    default Uni<T> findById(I id) {
        if (id == null) return Uni.createFrom().nullItem();
        return getHashCommands().hgetall(buildEntityKey(id)).map(map -> map.isEmpty() ? null : map.values().iterator().next());
    }

    @Override
    default Uni<List<T>> findById(List<I> ids) {
        if (ids == null || ids.isEmpty()) return Uni.createFrom().item(List.of());
        List<Uni<T>> lookups = ids.stream().map(this::findById).toList();
        return Uni.join().all(lookups).andFailFast().map(list -> list.stream().filter(java.util.Objects::nonNull).toList());
    }

    @Override
    default Uni<Boolean> existsById(I id) {
        if (id == null) return Uni.createFrom().item(false);
        return getRedisDataSource().key().exists(buildEntityKey(id));
    }

    @Override
    default Uni<List<Boolean>> existsById(List<I> ids) {
        if (ids == null || ids.isEmpty()) return Uni.createFrom().item(List.of());
        List<Uni<Boolean>> checks = ids.stream().map(this::existsById).toList();
        return Uni.join().all(checks).andFailFast();
    }

    @Override
    default Uni<T> save(T entity) {
        if (entity.getId() == null) return Uni.createFrom().failure(new IllegalArgumentException("Entity ID cannot be null"));
        return getHashCommands().hset(buildEntityKey(entity.getId()), "", entity).replaceWith(entity);
    }

    @Override
    default Uni<List<T>> save(List<T> entities) {
        if (entities == null || entities.isEmpty()) return Uni.createFrom().item(List.of());
        List<Uni<T>> saves = entities.stream().map(this::save).toList();
        return Uni.join().all(saves).andFailFast();
    }

    default Uni<T> save(T entity, Duration ttl) {
        if (ttl == null || ttl.isNegative() || ttl.isZero()) return save(entity);
        return save(entity).flatMap(savedEntity -> getRedisDataSource().key().expire(buildEntityKey(savedEntity.getId()), ttl).replaceWith(savedEntity));
    }

    default Uni<List<T>> save(List<T> entities, Duration ttl) {
        if (entities == null || entities.isEmpty()) return Uni.createFrom().item(List.of());
        if (ttl == null || ttl.isNegative() || ttl.isZero()) return save(entities);
        List<Uni<T>> savesWithTtl = entities.stream().map(e -> this.save(e, ttl)).toList();
        return Uni.join().all(savesWithTtl).andFailFast();
    }

    @Override
    default Uni<T> removeById(I id) {
        if (id == null) return Uni.createFrom().nullItem();
        return findById(id).flatMap(entity -> {
            if (entity == null) return Uni.createFrom().nullItem();
            return getRedisDataSource().key().del(buildEntityKey(id)).replaceWith(entity);
        });
    }

    @Override
    default Uni<List<T>> removeById(List<I> ids) {
        if (ids == null || ids.isEmpty()) return Uni.createFrom().item(List.of());
        return findById(ids).flatMap(entities -> {
            if (entities.isEmpty()) return Uni.createFrom().item(List.of());
            String[] keysToRemove = ids.stream().map(this::buildEntityKey).toArray(String[]::new);
            return getRedisDataSource().key().del(keysToRemove).replaceWith(entities);
        });
    }
}
