package com.egorgoncharov.krot.backend.application.events.delivery.dispatcher;

import com.egorgoncharov.krot.backend.application.context.model.EventContext;
import com.egorgoncharov.krot.backend.application.events.delivery.AbstractDeliveryManager;
import com.egorgoncharov.krot.backend.application.events.delivery.DeviceDeliveryManager;
import com.egorgoncharov.krot.backend.database.relational.entity.DeviceEntity;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@Startup
@ApplicationScoped
public class DeviceDeliveryDispatcher extends AbstractDeliveryDispatcher<DeviceEntity, UUID, Void> {
    private final DeviceDeliveryManager deliveryManager;

    @Inject
    public DeviceDeliveryDispatcher(DeviceDeliveryManager deliveryManager) {
        this.deliveryManager = deliveryManager;
    }

    public DeviceDeliveryDispatcher() {
        this(null);
    }

    @Override
    protected AbstractDeliveryManager<DeviceEntity, UUID, Void> deliveryManager() {
        return deliveryManager;
    }

    @Override
    public void dispatch(EventContext<DeviceEntity, UUID, Void> context) {
    }

    @PostConstruct
    @Override
    protected void start() {
        super.start();
    }
}
