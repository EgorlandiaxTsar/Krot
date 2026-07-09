package com.egorgoncharov.krot.backend.application.core.device;

import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.application.context.common.EntityCreationContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityDeleteContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityUpdateContext;
import com.egorgoncharov.krot.backend.application.service.AbstractModifyingService;
import com.egorgoncharov.krot.backend.database.relational.RelationalCrudRepository;
import com.egorgoncharov.krot.backend.database.relational.entity.DeviceCollaboratorEntity;
import com.egorgoncharov.krot.backend.database.relational.entity.DeviceEntity;
import com.egorgoncharov.krot.backend.database.relational.repository.DeviceCollaboratorRepository;
import com.egorgoncharov.krot.backend.database.relational.repository.DeviceRepository;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class DeviceService extends AbstractModifyingService<DeviceEntity, UUID, Void> {
    private final DeviceRepository repository;
    private final DeviceCollaboratorRepository collaboratorRepository;

    @Inject
    public DeviceService(DeviceRepository repository, DeviceCollaboratorRepository collaboratorRepository) {
        this.repository = repository;
        this.collaboratorRepository = collaboratorRepository;
    }

    @Override
    protected RelationalCrudRepository<DeviceEntity, UUID> repository() {
        return repository;
    }

    @WithTransaction
    @Override
    public Uni<Result<DeviceEntity>> create(EntityCreationContext<DeviceEntity, UUID, Void> context) {
        return repository.existsByName(context.getEntity().getName()).chain(exists -> exists ? Uni.createFrom().item(Result.conflict("name")) : super.create(context));
    }

    @WithTransaction
    @Override
    public Uni<Result<DeviceEntity>> update(EntityUpdateContext<DeviceEntity, UUID, Void> context) {
        Uni<Boolean> usernameExistsUni = (context.getNewEntity().getName() == null || context.getNewEntity().getName().equals(context.getOldEntity().getName())) ? Uni.createFrom().item(false) : repository.find("name = ?1 AND id != ?2", context.getNewEntity().getName(), context.getNewEntity().getId()).count().map(c -> c > 0);
        return usernameExistsUni.chain(exists -> exists ? Uni.createFrom().item(Result.conflict("name")) : super.update(context));
    }

    @WithTransaction
    @Override
    public Uni<Result<List<DeviceEntity>>> delete(EntityDeleteContext<DeviceEntity, UUID, Void> context) {
        Map<String, Object> parameters = new HashMap<>() {{
            put("ids", context.getEntities().stream().map(DeviceEntity::getId).toList());
        }};
        return repository.find("FROM DeviceEntity d WHERE d.id IN :ids AND (d.collaborators IS NOT EMPTY)", parameters).firstResult().chain(relations -> {
            if (relations != null) return Uni.createFrom().item(Result.badRequest("Some devices have collaborators, make sure to unlink them and try again"));
            return super.delete(context);
        });
    }

    @WithTransaction
    public Uni<Result<Void>> upsertCollaborator(EntityUpdateContext<DeviceEntity, UUID, Void> context) {
        DeviceCollaboratorEntity collaborator = context.getNewEntity().getCollaborators().getFirst();
        if (context.principal().getId().equals(collaborator.getId())) return Uni.createFrom().item(Result.badRequest("Cannot set owner as a collaborator"));
        DeviceCollaboratorEntity existingCollaborator = context.getOldEntity().getCollaborators().stream().filter(e -> e.getCollaborator().getId().equals(collaborator.getCollaborator().getId())).findAny().orElse(null);
        return collaboratorRepository.save(collaborator).chain(newCollaborator -> {
            Uni<Void> deleteCollaboratorUni = existingCollaborator != null ? collaboratorRepository.deleteById(existingCollaborator.getId()).replaceWithVoid() : Uni.createFrom().voidItem();
            return deleteCollaboratorUni.replaceWith(Result::ok);
        });
    }

    @WithTransaction
    public Uni<Result<Void>> deleteCollaborator(EntityUpdateContext<DeviceEntity, UUID, Void> context) {
        DeviceCollaboratorEntity collaborator = context.getNewEntity().getCollaborators().getFirst();
        return collaboratorRepository.removeById(collaborator.getId()).chain(() -> Result.ok().voidCast().toUni());
    }

    @WithTransaction
    public Uni<Result<Void>> transferOwnership(EntityUpdateContext<DeviceEntity, UUID, Void> context) {
        DeviceCollaboratorEntity newOwnerAsCollaborator = context.getNewEntity().getCollaborators().stream().filter(collaborator -> collaborator.getCollaborator().getId().equals(context.getNewEntity().getOwner().getId())).findAny().orElse(null);
        Uni<Void> newOwnerAsCollaboratorDeleteUni = newOwnerAsCollaborator == null ? Uni.createFrom().voidItem() : collaboratorRepository.removeById(newOwnerAsCollaborator.getId()).replaceWithVoid();
        return newOwnerAsCollaboratorDeleteUni.chain(() -> super.update(context)).map(Result::voidCast);
    }
}
