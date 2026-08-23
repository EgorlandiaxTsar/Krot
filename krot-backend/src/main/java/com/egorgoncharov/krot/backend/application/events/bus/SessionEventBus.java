package com.egorgoncharov.krot.backend.application.events.bus;

import com.egorgoncharov.krot.backend.database.relational.entity.HistoricalSessionEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class SessionEventBus extends AbstractEventBus<HistoricalSessionEntity, UUID, Void> {
}
