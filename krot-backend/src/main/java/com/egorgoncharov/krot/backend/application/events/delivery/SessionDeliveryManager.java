package com.egorgoncharov.krot.backend.application.events.delivery;

import com.egorgoncharov.krot.backend.database.relational.entity.HistoricalSessionEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class SessionDeliveryManager extends AbstractDeliveryManager<HistoricalSessionEntity, UUID, Void> {
}
