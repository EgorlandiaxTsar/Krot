package com.egorgoncharov.krot.backend.application.service;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.application.context.model.CreationContext;
import com.egorgoncharov.krot.backend.application.context.model.DeleteContext;
import com.egorgoncharov.krot.backend.application.context.model.UpdateContext;
import com.egorgoncharov.krot.backend.application.service.modification.AbstractModificationService;
import com.egorgoncharov.krot.backend.database.relational.RelationalCrudRepository;
import com.egorgoncharov.krot.backend.database.relational.entity.ProgramCollaboratorEntity;
import com.egorgoncharov.krot.backend.database.relational.entity.ProgramEntity;
import com.egorgoncharov.krot.backend.database.relational.repository.ProgramCollaboratorRepository;
import com.egorgoncharov.krot.backend.database.relational.repository.ProgramRepository;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class ProgramService extends AbstractModificationService<ProgramEntity, UUID, Void> {
    private final ProgramRepository repository;
    private final ProgramCollaboratorRepository collaboratorRepository;

    @Inject
    public ProgramService(ProgramRepository repository, ProgramCollaboratorRepository collaboratorRepository) {
        this.repository = repository;
        this.collaboratorRepository = collaboratorRepository;
    }

    @Override
    protected RelationalCrudRepository<ProgramEntity, UUID> repository() {
        return repository;
    }

    @WithTransaction
    @Override
    public Uni<Result<ProgramEntity>> create(CreationContext<ProgramEntity, UUID, Void> context) {
        return repository.existsByName(context.getEntity().getName()).chain(exists -> exists ? Uni.createFrom().item(Result.conflict("name")) : super.create(context));
    }

    @WithTransaction
    @Override
    public Uni<Result<ProgramEntity>> update(UpdateContext<ProgramEntity, UUID, Void> context) {
        Uni<Boolean> nameExistsUni = (context.getNewEntity().getName() == null || context.getNewEntity().getName().equals(context.getOldEntity().getName())) ? Uni.createFrom().item(false) : repository.find("name = ?1 AND id != ?2", context.getNewEntity().getName(), context.getNewEntity().getId()).count().map(c -> c > 0);
        return nameExistsUni.chain(exists -> exists ? Uni.createFrom().item(Result.conflict("name")) : super.update(context));
    }

    @WithTransaction
    @Override
    public Uni<Result<List<ProgramEntity>>> delete(DeleteContext<ProgramEntity, UUID, Void> context) {
        Map<String, Object> parameters = new HashMap<>() {{
            put("ids", context.getEntities().stream().map(ProgramEntity::getId).toList());
        }};
        return repository.find("FROM ProgramEntity p WHERE p.id IN :ids AND (p.collaborators IS NOT EMPTY)", parameters).firstResult().chain(relations -> {
            if (relations != null) return Uni.createFrom().item(Result.badRequest("Some programs have collaborators, make sure to unlink them and try again"));
            return super.delete(context);
        });
    }

    @WithTransaction
    public Uni<Result<Void>> upsertCollaborator(UpdateContext<ProgramEntity, UUID, Void> context) {
        ProgramCollaboratorEntity collaborator = context.getNewEntity().getCollaborators().getFirst();
        if (context.principal().getId().equals(collaborator.getId())) return Uni.createFrom().item(Result.badRequest("Cannot set owner as a collaborator"));
        ProgramCollaboratorEntity existingCollaborator = context.getOldEntity().getCollaborators().stream().filter(e -> e.getCollaborator().getId().equals(collaborator.getCollaborator().getId())).findAny().orElse(null);
        return collaboratorRepository.save(collaborator).chain(newCollaborator -> {
            Uni<Void> deleteCollaboratorUni = existingCollaborator != null ? collaboratorRepository.deleteById(existingCollaborator.getId()).replaceWithVoid() : Uni.createFrom().voidItem();
            return deleteCollaboratorUni.replaceWith(Result::ok);
        });
    }

    @WithTransaction
    public Uni<Result<Void>> deleteCollaborator(UpdateContext<ProgramEntity, UUID, Void> context) {
        ProgramCollaboratorEntity collaborator = context.getNewEntity().getCollaborators().getFirst();
        return collaboratorRepository.removeById(collaborator.getId()).chain(() -> Result.ok().voidCast().toUni());
    }

    @WithTransaction
    public Uni<Result<Void>> transferOwnership(UpdateContext<ProgramEntity, UUID, Void> context) {
        ProgramCollaboratorEntity newOwnerAsCollaborator = context.getNewEntity().getCollaborators().stream().filter(collaborator -> collaborator.getCollaborator().getId().equals(context.getNewEntity().getOwner().getId())).findAny().orElse(null);
        Uni<Void> newOwnerAsCollaboratorDeleteUni = newOwnerAsCollaborator == null ? Uni.createFrom().voidItem() : collaboratorRepository.removeById(newOwnerAsCollaborator.getId()).replaceWithVoid();
        return newOwnerAsCollaboratorDeleteUni.chain(() -> super.update(context)).map(Result::voidCast);
    }
}
