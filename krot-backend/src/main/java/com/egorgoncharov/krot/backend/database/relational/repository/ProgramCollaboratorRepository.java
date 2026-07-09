package com.egorgoncharov.krot.backend.database.relational.repository;

import com.egorgoncharov.krot.backend.database.relational.RelationalCrudRepository;
import com.egorgoncharov.krot.backend.database.relational.entity.ProgramCollaboratorEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class ProgramCollaboratorRepository implements RelationalCrudRepository<ProgramCollaboratorEntity, UUID> {
}
