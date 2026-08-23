package com.egorgoncharov.krot.backend.api.ws.eventstream.dispatcher;

import com.egorgoncharov.krot.backend.api.model.response.ApiEvent;
import com.egorgoncharov.krot.backend.api.ws.eventstream.WebSocketEventStreamPublisher;
import com.egorgoncharov.krot.backend.application.events.delivery.dispatcher.outbound.OutboundDeliveryResolver;
import com.egorgoncharov.krot.backend.database.Identifiable;

import java.util.List;
import java.util.UUID;

public interface WebSocketOutboundDeliveryResolver<T extends Identifiable<I>, I, A> extends OutboundDeliveryResolver<T, I, A> {
    WebSocketEventStreamPublisher publisher();

    @Override
    default void send(ApiEvent<T, I, A> event, List<UUID> userIds) {
        publisher().push(event, userIds);
    }
}
