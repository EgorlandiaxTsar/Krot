package com.egorgoncharov.krot.backend.api.ws.eventstream.dispatcher;

import com.egorgoncharov.krot.backend.api.model.response.user.User;
import com.egorgoncharov.krot.backend.api.ws.eventstream.WebSocketEventStreamPublisher;
import com.egorgoncharov.krot.backend.application.events.delivery.AbstractDeliveryManager;
import com.egorgoncharov.krot.backend.application.events.delivery.UserDeliveryManager;
import com.egorgoncharov.krot.backend.application.events.delivery.dispatcher.outbound.AbstractUserOutboundDeliveryDispatcher;
import com.egorgoncharov.krot.backend.application.guard.view.UserViewGuard;
import com.egorgoncharov.krot.backend.application.guard.view.ViewGuard;
import com.egorgoncharov.krot.backend.connection.ApplicationConnectionRegistry;
import com.egorgoncharov.krot.backend.connection.ConnectionRegistry;
import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@Startup
@ApplicationScoped
public class WebSocketUserOutboundDeliveryDispatcher extends AbstractUserOutboundDeliveryDispatcher implements WebSocketOutboundDeliveryResolver<UserEntity, UUID, Void> {
    private final ApplicationConnectionRegistry connectionRegistry;
    private final WebSocketEventStreamPublisher publisher;
    private final UserViewGuard viewGuard;
    private final UserDeliveryManager deliveryManager;

    @Inject
    public WebSocketUserOutboundDeliveryDispatcher(ApplicationConnectionRegistry connectionRegistry, WebSocketEventStreamPublisher publisher, UserViewGuard viewGuard, UserDeliveryManager deliveryManager) {
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
    protected ViewGuard<UserEntity, UUID, Void> viewGuard() {
        return viewGuard;
    }

    @Override
    protected User entityReflectionConverter() {
        return new User();
    }

    @Override
    protected AbstractDeliveryManager<UserEntity, UUID, Void> deliveryManager() {
        return deliveryManager;
    }

    @PostConstruct
    @Override
    protected void start() {
        super.start();
    }
}
