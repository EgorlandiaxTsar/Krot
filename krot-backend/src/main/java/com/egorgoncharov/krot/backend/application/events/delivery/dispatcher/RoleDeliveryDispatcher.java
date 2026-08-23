package com.egorgoncharov.krot.backend.application.events.delivery.dispatcher;

import com.egorgoncharov.krot.backend.application.context.model.EventContext;
import com.egorgoncharov.krot.backend.application.events.delivery.AbstractDeliveryManager;
import com.egorgoncharov.krot.backend.application.events.delivery.RoleDeliveryManager;
import com.egorgoncharov.krot.backend.database.relational.entity.RoleEntity;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@Startup
@ApplicationScoped
public class RoleDeliveryDispatcher extends AbstractDeliveryDispatcher<RoleEntity, UUID, Void> {
    private final RoleDeliveryManager deliveryManager;

    @Inject
    public RoleDeliveryDispatcher(RoleDeliveryManager deliveryManager) {
        this.deliveryManager = deliveryManager;
    }

    public RoleDeliveryDispatcher() {
        this(null);
    }

    @Override
    protected AbstractDeliveryManager<RoleEntity, UUID, Void> deliveryManager() {
        return deliveryManager;
    }

    @Override
    public void dispatch(EventContext<RoleEntity, UUID, Void> context) {
    }

    @PostConstruct
    @Override
    protected void start() {
        super.start();
    }
}
