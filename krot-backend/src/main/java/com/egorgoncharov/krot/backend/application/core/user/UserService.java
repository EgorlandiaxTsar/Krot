package com.egorgoncharov.krot.backend.application.core.user;

import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.application.context.common.EntityCreationContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityDeleteContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityUpdateContext;
import com.egorgoncharov.krot.backend.application.service.AbstractModifyingService;
import com.egorgoncharov.krot.backend.database.relational.RelationalCrudRepository;
import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
import com.egorgoncharov.krot.backend.database.relational.repository.HistoricalSessionRepository;
import com.egorgoncharov.krot.backend.database.relational.repository.UserRepository;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class UserService extends AbstractModifyingService<UserEntity, UUID, Void> {
    private final UserRepository repository;
    private final HistoricalSessionRepository sessionRepository;

    @Inject
    public UserService(UserRepository repository, HistoricalSessionRepository sessionRepository) {
        this.repository = repository;
        this.sessionRepository = sessionRepository;
    }

    @Override
    protected RelationalCrudRepository<UserEntity, UUID> repository() {
        return repository;
    }

    @WithTransaction
    @Override
    public Uni<Result<UserEntity>> create(EntityCreationContext<UserEntity, UUID, Void> context) {
        return repository.existsByName(context.getEntity().getUsername()).chain(exists -> {
            if (exists) return Uni.createFrom().item(Result.conflict("username"));
            return super.create(context);
        });
    }

    @WithTransaction
    @Override
    public Uni<Result<UserEntity>> update(EntityUpdateContext<UserEntity, UUID, Void> context) {
        Uni<Boolean> usernameExistsUni = (context.getNewEntity().getUsername() == null || context.getNewEntity().getUsername().equals(context.getOldEntity().getUsername())) ? Uni.createFrom().item(false) : repository.find("username = ?1 AND id != ?2", context.getNewEntity().getUsername(), context.getNewEntity().getId()).count().map(c -> c > 0);
        return usernameExistsUni.chain(exists -> {
            if (exists) return Uni.createFrom().item(Result.conflict("username"));
            return super.update(context);
        });
    }

    @WithTransaction
    @Override
    public Uni<Result<List<UserEntity>>> delete(EntityDeleteContext<UserEntity, UUID, Void> context) {
        Map<String, Object> parameters = new HashMap<>() {{
            put("ids", context.getEntities().stream().map(UserEntity::getId).toList());
        }};
        return repository.find("FROM UserEntity u WHERE u.id IN :ids AND (u.programs IS NOT EMPTY OR u.devices IS NOT EMPTY)", parameters).firstResult().chain(relations -> {
            if (relations != null) return Uni.createFrom().item(Result.badRequest("Some users own devices and/or programs, make sure to unlink these entities and try again"));
            return sessionRepository.delete("userOwner.id IN :ids", parameters).chain(() -> super.delete(context));
        });
    }

    @WithTransaction
    public Uni<Result<Void>> updatePassword(EntityUpdateContext<UserEntity, UUID, Void> context) {
        return super.update(context).map(Result::voidCast);
    }
}
