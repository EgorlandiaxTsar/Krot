package com.egorgoncharov.krot.backend.model.repository;

import com.egorgoncharov.krot.backend.model.common.ReactiveRepository;
import com.egorgoncharov.krot.backend.model.entity.SessionEntity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SessionRepository implements ReactiveRepository<SessionEntity, UUID> {
    public Uni<SessionEntity> findBySrcKey(UUID srcKey) {
        return findBy(srcKey, "srcKey");
    }

    public Uni<List<SessionEntity>> findBySrcKey(List<UUID> srcKeys) {
        return findBy(srcKeys, "srcKey");
    }

    /*
     * The technical choice to store sessions inside a robust PostgreSQL database, instead of quick
     * Redis or MongoDB option, comes with the need of monitoring sessions history in order to
     * protect the app from malicious cyberattacks. Therefore, it's important to keep history of
     * all ever created sessions, including the deleted ones. That's why, this specific repository
     * doesn't delete sessions physically, instead, it invalidates them by setting a minimal valid
     * until time.
     * */
    @Override
    public Uni<List<SessionEntity>> removeById(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) return Uni.createFrom().item(List.of());
        return findById(ids).flatMap(items -> {
            if (items.isEmpty()) return Uni.createFrom().item(List.of());
            items.forEach(item -> item.setValidUntil(OffsetDateTime.MIN));
            return save(items);
        });
    }
}
