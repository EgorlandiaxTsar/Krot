package com.egorgoncharov.krot.backend.application.events.delivery.dispatcher.outbound;

import com.egorgoncharov.krot.backend.api.model.response.ApiEvent;
import com.egorgoncharov.krot.backend.database.Identifiable;

import java.util.List;
import java.util.UUID;

public interface OutboundDeliveryResolver<T extends Identifiable<I>, I, A> {
    void send(ApiEvent<T, I, A> event, List<UUID> userIds);
}
