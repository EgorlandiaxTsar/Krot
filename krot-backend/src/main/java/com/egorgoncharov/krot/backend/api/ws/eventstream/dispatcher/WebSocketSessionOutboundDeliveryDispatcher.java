package com.egorgoncharov.krot.backend.api.ws.eventstream.dispatcher;

import com.egorgoncharov.krot.backend.api.model.response.session.Session;
import com.egorgoncharov.krot.backend.api.ws.eventstream.WebSocketEventStreamPublisher;
import com.egorgoncharov.krot.backend.application.events.delivery.AbstractDeliveryManager;
import com.egorgoncharov.krot.backend.application.events.delivery.SessionDeliveryManager;
import com.egorgoncharov.krot.backend.application.events.delivery.dispatcher.outbound.AbstractSessionOutboundDeliveryDispatcher;
import com.egorgoncharov.krot.backend.application.guard.view.SessionViewGuard;
import com.egorgoncharov.krot.backend.application.guard.view.ViewGuard;
import com.egorgoncharov.krot.backend.connection.ApplicationConnectionRegistry;
import com.egorgoncharov.krot.backend.connection.ConnectionRegistry;
import com.egorgoncharov.krot.backend.database.relational.entity.HistoricalSessionEntity;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@Startup
@ApplicationScoped
public class WebSocketSessionOutboundDeliveryDispatcher extends AbstractSessionOutboundDeliveryDispatcher implements WebSocketOutboundDeliveryResolver<HistoricalSessionEntity, UUID, Void> {
    private final ApplicationConnectionRegistry connectionRegistry;
    private final WebSocketEventStreamPublisher publisher;
    private final SessionViewGuard viewGuard;
    private final SessionDeliveryManager deliveryManager;

    @Inject
    public WebSocketSessionOutboundDeliveryDispatcher(ApplicationConnectionRegistry connectionRegistry, WebSocketEventStreamPublisher publisher, SessionViewGuard viewGuard, SessionDeliveryManager deliveryManager) {
        this.connectionRegistry = connectionRegistry;
        this.publisher = publisher;
        this.viewGuard = viewGuard;
        this.deliveryManager = deliveryManager;
    }

    @Override
    public WebSocketEventStreamPublisher publisher() {
        return publisher;
    }

    @Override
    protected ConnectionRegistry connectionRegistry() {
        return connectionRegistry;
    }

    @Override
    protected ViewGuard<HistoricalSessionEntity, UUID, Void> viewGuard() {
        return viewGuard;
    }

    @Override
    protected Session entityReflectionConverter() {
        return new Session();
    }

    @Override
    protected AbstractDeliveryManager<HistoricalSessionEntity, UUID, Void> deliveryManager() {
        return deliveryManager;
    }

    @PostConstruct
    @Override
    protected void start() {
        super.start();
    }
}
