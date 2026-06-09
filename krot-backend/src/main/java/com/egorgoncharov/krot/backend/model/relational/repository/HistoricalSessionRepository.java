package com.egorgoncharov.krot.backend.model.relational.repository;

import com.egorgoncharov.krot.backend.model.relational.RelationalReactiveRepository;
import com.egorgoncharov.krot.backend.model.relational.entity.HistoricalSessionEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class HistoricalSessionRepository implements RelationalReactiveRepository<HistoricalSessionEntity, UUID> {
}
