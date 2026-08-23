package com.egorgoncharov.krot.backend.connection;

import com.egorgoncharov.krot.backend.application.context.model.EventContext;
import com.egorgoncharov.krot.backend.application.events.EventType;
import com.egorgoncharov.krot.backend.application.events.delivery.UserDeliveryManager;
import com.egorgoncharov.krot.backend.application.events.delivery.dispatcher.UserDeliveryDispatcher;
import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@Startup
@ApplicationScoped
public class ConnectionRegistryUserUpdater extends UserDeliveryDispatcher {
    private final ApplicationConnectionRegistry connectionRegistry;

    @Inject
    public ConnectionRegistryUserUpdater(UserDeliveryManager deliveryManager, ApplicationConnectionRegistry connectionRegistry) {
        super(deliveryManager);
        this.connectionRegistry = connectionRegistry;
    }

    @Override
    public void dispatch(EventContext<UserEntity, UUID, Void> context) {
        if (context.getEvent() != EventType.UPDATED) return;
        connectionRegistry.updateConnection(ConnectionMetadata.from(context.targets().getFirst()));
    }
}
