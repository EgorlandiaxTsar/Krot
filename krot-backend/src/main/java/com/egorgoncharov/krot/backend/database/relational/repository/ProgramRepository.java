package com.egorgoncharov.krot.backend.database.relational.repository;

import com.egorgoncharov.krot.backend.database.relational.RelationalNameableRepository;
import com.egorgoncharov.krot.backend.database.relational.entity.ProgramEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class ProgramRepository implements RelationalNameableRepository<ProgramEntity, UUID> {
}
