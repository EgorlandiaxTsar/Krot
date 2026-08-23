package com.egorgoncharov.krot.backend.api.ws.eventstream.dispatcher;

import com.egorgoncharov.krot.backend.api.model.response.device.Device;
import com.egorgoncharov.krot.backend.api.ws.eventstream.WebSocketEventStreamPublisher;
import com.egorgoncharov.krot.backend.application.events.delivery.AbstractDeliveryManager;
import com.egorgoncharov.krot.backend.application.events.delivery.DeviceDeliveryManager;
import com.egorgoncharov.krot.backend.application.events.delivery.dispatcher.outbound.AbstractDeviceOutboundDeliveryDispatcher;
import com.egorgoncharov.krot.backend.application.guard.view.DeviceViewGuard;
import com.egorgoncharov.krot.backend.application.guard.view.ViewGuard;
import com.egorgoncharov.krot.backend.connection.ApplicationConnectionRegistry;
import com.egorgoncharov.krot.backend.connection.ConnectionRegistry;
import com.egorgoncharov.krot.backend.database.relational.entity.DeviceEntity;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@Startup
@ApplicationScoped
public class WebSocketDeviceOutboundDeliveryDispatcher extends AbstractDeviceOutboundDeliveryDispatcher implements WebSocketOutboundDeliveryResolver<DeviceEntity, UUID, Void> {
    private final ApplicationConnectionRegistry connectionRegistry;
    private final WebSocketEventStreamPublisher publisher;
    private final DeviceViewGuard viewGuard;
    private final DeviceDeliveryManager deliveryManager;

    @Inject
    public WebSocketDeviceOutboundDeliveryDispatcher(ApplicationConnectionRegistry connectionRegistry, WebSocketEventStreamPublisher publisher, DeviceViewGuard viewGuard, DeviceDeliveryManager deliveryManager) {
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
    protected ViewGuard<DeviceEntity, UUID, Void> viewGuard() {
        return viewGuard;
    }

    @Override
    protected Device entityReflectionConverter() {
        return new Device();
    }

    @Override
    protected AbstractDeliveryManager<DeviceEntity, UUID, Void> deliveryManager() {
        return deliveryManager;
    }

    @PostConstruct
    @Override
    protected void start() {
        super.start();
    }
}
