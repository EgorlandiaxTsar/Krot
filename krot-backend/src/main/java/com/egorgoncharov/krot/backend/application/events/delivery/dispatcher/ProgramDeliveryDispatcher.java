package com.egorgoncharov.krot.backend.application.events.delivery.dispatcher;

import com.egorgoncharov.krot.backend.application.context.model.EventContext;
import com.egorgoncharov.krot.backend.application.events.delivery.AbstractDeliveryManager;
import com.egorgoncharov.krot.backend.application.events.delivery.ProgramDeliveryManager;
import com.egorgoncharov.krot.backend.database.relational.entity.ProgramEntity;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@Startup
@ApplicationScoped
public class ProgramDeliveryDispatcher extends AbstractDeliveryDispatcher<ProgramEntity, UUID, Void> {
    private final ProgramDeliveryManager deliveryManager;

    @Inject
    public ProgramDeliveryDispatcher(ProgramDeliveryManager deliveryManager) {
        this.deliveryManager = deliveryManager;
    }

    public ProgramDeliveryDispatcher() {
        this(null);
    }

    @Override
    protected AbstractDeliveryManager<ProgramEntity, UUID, Void> deliveryManager() {
        return deliveryManager;
    }

    @Override
    public void dispatch(EventContext<ProgramEntity, UUID, Void> context) {
    }

    @PostConstruct
    @Override
    protected void start() {
        super.start();
    }
}
