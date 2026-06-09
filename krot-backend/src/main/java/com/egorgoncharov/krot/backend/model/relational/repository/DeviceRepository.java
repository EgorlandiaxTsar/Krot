package com.egorgoncharov.krot.backend.model.relational.repository;

import com.egorgoncharov.krot.backend.model.relational.RelationalNameableRepository;
import com.egorgoncharov.krot.backend.model.relational.entity.DeviceEntity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class DeviceRepository implements RelationalNameableRepository<DeviceEntity, UUID> {
    @Inject
    UserRepository userRepository;

    @Override
    public Uni<DeviceEntity> save(DeviceEntity entity) {
        if (isPersistent(entity)) return Uni.createFrom().item(entity);
        return generateUniqueId().chain(uniqueId -> {
            entity.setId(uniqueId);
            return getSession().chain(s -> s.merge(entity));
        });
    }

    /* Recursively generates a UUID and checks if it exists in the user repository. If it exists,
     * the method calls itself again to try a new one.
     */
    private Uni<UUID> generateUniqueId() {
        UUID candidateId = UUID.randomUUID();
        return userRepository.existsById(candidateId).chain(exists -> {
            if (exists) {
                return generateUniqueId();
            } else {
                return Uni.createFrom().item(candidateId);
            }
        });
    }
}
