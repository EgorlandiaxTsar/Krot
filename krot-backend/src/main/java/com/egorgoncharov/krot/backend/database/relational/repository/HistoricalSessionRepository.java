package com.egorgoncharov.krot.backend.database.relational.repository;

import com.egorgoncharov.krot.backend.database.relational.RelationalCrudRepository;
import com.egorgoncharov.krot.backend.database.relational.entity.HistoricalSessionEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class HistoricalSessionRepository implements RelationalCrudRepository<HistoricalSessionEntity, UUID> {
}
