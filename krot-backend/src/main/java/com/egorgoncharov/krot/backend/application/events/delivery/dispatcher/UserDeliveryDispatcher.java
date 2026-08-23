package com.egorgoncharov.krot.backend.application.events.delivery.dispatcher;

import com.egorgoncharov.krot.backend.application.context.model.EventContext;
import com.egorgoncharov.krot.backend.application.events.delivery.AbstractDeliveryManager;
import com.egorgoncharov.krot.backend.application.events.delivery.UserDeliveryManager;
import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@Startup
@ApplicationScoped
public class UserDeliveryDispatcher extends AbstractDeliveryDispatcher<UserEntity, UUID, Void> {
    private final UserDeliveryManager deliveryManager;

    @Inject
    public UserDeliveryDispatcher(UserDeliveryManager deliveryManager) {
        this.deliveryManager = deliveryManager;
    }

    public UserDeliveryDispatcher() {
        this(null);
    }

    @Override
    protected AbstractDeliveryManager<UserEntity, UUID, Void> deliveryManager() {
        return deliveryManager;
    }

    @Override
    public void dispatch(EventContext<UserEntity, UUID, Void> context) {
    }

    @PostConstruct
    @Override
    protected void start() {
        super.start();
    }
}
