package com.egorgoncharov.krot.backend.model.repository;

import com.egorgoncharov.krot.backend.model.common.NameableEntityRepository;
import com.egorgoncharov.krot.backend.model.entity.ProgramEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class ProgramRepository implements NameableEntityRepository<ProgramEntity, UUID> {
}
