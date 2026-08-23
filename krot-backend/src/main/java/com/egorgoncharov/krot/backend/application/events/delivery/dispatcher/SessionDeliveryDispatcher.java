package com.egorgoncharov.krot.backend.application.events.delivery.dispatcher;

import com.egorgoncharov.krot.backend.application.context.model.EventContext;
import com.egorgoncharov.krot.backend.application.events.delivery.AbstractDeliveryManager;
import com.egorgoncharov.krot.backend.application.events.delivery.SessionDeliveryManager;
import com.egorgoncharov.krot.backend.database.relational.entity.HistoricalSessionEntity;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@Startup
@ApplicationScoped
public class SessionDeliveryDispatcher extends AbstractDeliveryDispatcher<HistoricalSessionEntity, UUID, Void> {
    private final SessionDeliveryManager deliveryManager;

    @Inject
    public SessionDeliveryDispatcher(SessionDeliveryManager deliveryManager) {
        this.deliveryManager = deliveryManager;
    }

    public SessionDeliveryDispatcher() {
        this(null);
    }

    @Override
    protected AbstractDeliveryManager<HistoricalSessionEntity, UUID, Void> deliveryManager() {
        return deliveryManager;
    }

    @Override
    public void dispatch(EventContext<HistoricalSessionEntity, UUID, Void> context) {
    }

    @PostConstruct
    @Override
    protected void start() {
        super.start();
    }
}
