package com.egorgoncharov.krot.backend.api.ws.eventstream.dispatcher;

import com.egorgoncharov.krot.backend.api.model.response.role.Role;
import com.egorgoncharov.krot.backend.api.ws.eventstream.WebSocketEventStreamPublisher;
import com.egorgoncharov.krot.backend.application.events.delivery.AbstractDeliveryManager;
import com.egorgoncharov.krot.backend.application.events.delivery.RoleDeliveryManager;
import com.egorgoncharov.krot.backend.application.events.delivery.dispatcher.outbound.AbstractRoleOutboundDeliveryDispatcher;
import com.egorgoncharov.krot.backend.application.guard.view.RoleViewGuard;
import com.egorgoncharov.krot.backend.application.guard.view.ViewGuard;
import com.egorgoncharov.krot.backend.connection.ApplicationConnectionRegistry;
import com.egorgoncharov.krot.backend.connection.ConnectionRegistry;
import com.egorgoncharov.krot.backend.database.relational.entity.RoleEntity;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@Startup
@ApplicationScoped
public class WebSocketRoleOutboundDeliveryDispatcher extends AbstractRoleOutboundDeliveryDispatcher implements WebSocketOutboundDeliveryResolver<RoleEntity, UUID, Void> {
    private final ApplicationConnectionRegistry connectionRegistry;
    private final WebSocketEventStreamPublisher publisher;
    private final RoleViewGuard viewGuard;
    private final RoleDeliveryManager deliveryManager;

    @Inject
    public WebSocketRoleOutboundDeliveryDispatcher(ApplicationConnectionRegistry connectionRegistry, WebSocketEventStreamPublisher publisher, RoleViewGuard viewGuard, RoleDeliveryManager deliveryManager) {
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
    protected ViewGuard<RoleEntity, UUID, Void> viewGuard() {
        return viewGuard;
    }

    @Override
    protected Role entityReflectionConverter() {
        return new Role();
    }

    @Override
    protected AbstractDeliveryManager<RoleEntity, UUID, Void> deliveryManager() {
        return deliveryManager;
    }

    @PostConstruct
    @Override
    protected void start() {
        super.start();
    }
}
