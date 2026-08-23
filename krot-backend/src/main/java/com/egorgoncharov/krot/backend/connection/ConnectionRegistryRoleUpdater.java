package com.egorgoncharov.krot.backend.connection;

import com.egorgoncharov.krot.backend.application.context.model.EventContext;
import com.egorgoncharov.krot.backend.application.events.EventType;
import com.egorgoncharov.krot.backend.application.events.delivery.RoleDeliveryManager;
import com.egorgoncharov.krot.backend.application.events.delivery.dispatcher.RoleDeliveryDispatcher;
import com.egorgoncharov.krot.backend.database.relational.entity.RoleEntity;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@Startup
@ApplicationScoped
public class ConnectionRegistryRoleUpdater extends RoleDeliveryDispatcher {
    private final ApplicationConnectionRegistry connectionRegistry;

    @Inject
    public ConnectionRegistryRoleUpdater(RoleDeliveryManager deliveryManager, ApplicationConnectionRegistry connectionRegistry) {
        super(deliveryManager);
        this.connectionRegistry = connectionRegistry;
    }

    @Override
    public void dispatch(EventContext<RoleEntity, UUID, Void> context) {
        if (context.getEvent() != EventType.UPDATED) return;
        connectionRegistry.updateRoles(context.targets().getFirst());
    }
}
