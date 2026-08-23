package com.egorgoncharov.krot.backend.application.events.delivery.dispatcher.outbound;

import com.egorgoncharov.krot.backend.api.model.response.session.Session;
import com.egorgoncharov.krot.backend.database.relational.entity.HistoricalSessionEntity;

import java.util.UUID;

public abstract class AbstractSessionOutboundDeliveryDispatcher extends AbstractOutboundDeliveryDispatcher<HistoricalSessionEntity, UUID, Void, Session> {
}
