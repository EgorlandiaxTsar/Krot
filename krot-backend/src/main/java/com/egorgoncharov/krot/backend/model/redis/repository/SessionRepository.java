package com.egorgoncharov.krot.backend.model.redis.repository;

import com.egorgoncharov.krot.backend.model.redis.RedisReactiveRepository;
import com.egorgoncharov.krot.backend.model.redis.entity.SessionEntity;
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.hash.ReactiveHashCommands;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@ApplicationScoped
public class SessionRepository implements RedisReactiveRepository<SessionEntity, UUID> {
    public static final String NAMESPACE = "krot:sessions";

    private final ReactiveRedisDataSource rds;
    private final ReactiveHashCommands<String, String, SessionEntity> hashCommands;

    @Inject
    public SessionRepository(ReactiveRedisDataSource rds) {
        this.rds = rds;
        this.hashCommands = rds.hash(SessionEntity.class);
    }

    @Override
    public String getRedisNamespace() {
        return NAMESPACE;
    }

    @Override
    public ReactiveHashCommands<String, String, SessionEntity> getHashCommands() {
        return hashCommands;
    }

    @Override
    public ReactiveRedisDataSource getRedisDataSource() {
        return rds;
    }

    private String buildReferenceIndexKey(UUID sessionReference) {
        return NAMESPACE + ":by-reference:" + sessionReference.toString();
    }

    public Uni<SessionEntity> findBySessionReference(UUID sessionReference) {
        if (sessionReference == null) return Uni.createFrom().nullItem();
        return rds.value(String.class).get(buildReferenceIndexKey(sessionReference)).flatMap(id -> {
            if (id == null) return Uni.createFrom().nullItem();
            return findById(UUID.fromString(id));
        });
    }

    public Uni<List<SessionEntity>> findBySessionReference(List<UUID> sessionReferences) {
        if (sessionReferences == null || sessionReferences.isEmpty()) return Uni.createFrom().item(List.of());
        List<Uni<SessionEntity>> lookups = sessionReferences.stream().map(this::findBySessionReference).toList();
        return Uni.join().all(lookups).andFailFast().map(list -> list.stream().filter(Objects::nonNull).toList());
    }

    @Override
    public Uni<SessionEntity> save(SessionEntity entity) {
        return RedisReactiveRepository.super.save(entity).flatMap(savedEntity -> {
            if (savedEntity.getSessionReference() == null) return Uni.createFrom().item(savedEntity);
            String indexKey = buildReferenceIndexKey(savedEntity.getSessionReference());
            return rds.value(String.class).set(indexKey, savedEntity.getId().toString()).replaceWith(savedEntity);
        });
    }

    @Override
    public Uni<SessionEntity> save(SessionEntity entity, Duration ttl) {
        return RedisReactiveRepository.super.save(entity, ttl).flatMap(savedEntity -> {
            if (savedEntity.getSessionReference() == null) return Uni.createFrom().item(savedEntity);
            String indexKey = buildReferenceIndexKey(savedEntity.getSessionReference());
            if (ttl != null && !ttl.isNegative() && !ttl.isZero()) return rds.value(String.class).setex(indexKey, ttl.toSeconds(), entity.getId().toString()).replaceWith(savedEntity);
            return rds.value(String.class).set(indexKey, savedEntity.getId().toString()).replaceWith(savedEntity);
        });
    }

    @Override
    public Uni<SessionEntity> removeById(UUID id) {
        return findById(id).flatMap(entity -> {
            if (entity == null) return Uni.createFrom().nullItem();
            Uni<Integer> cleanTarget = rds.key().del(buildEntityKey(id));
            if (entity.getSessionReference() != null) {
                Uni<Integer> cleanIndex = rds.key().del(buildReferenceIndexKey(entity.getSessionReference()));
                return Uni.join().all(cleanTarget, cleanIndex).andFailFast().replaceWith(entity);
            }
            return cleanTarget.replaceWith(entity);
        });
    }
}
