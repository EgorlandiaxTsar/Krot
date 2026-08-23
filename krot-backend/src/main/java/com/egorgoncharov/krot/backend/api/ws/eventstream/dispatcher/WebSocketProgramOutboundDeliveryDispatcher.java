package com.egorgoncharov.krot.backend.api.ws.eventstream.dispatcher;

import com.egorgoncharov.krot.backend.api.model.response.program.Program;
import com.egorgoncharov.krot.backend.api.ws.eventstream.WebSocketEventStreamPublisher;
import com.egorgoncharov.krot.backend.application.events.delivery.AbstractDeliveryManager;
import com.egorgoncharov.krot.backend.application.events.delivery.ProgramDeliveryManager;
import com.egorgoncharov.krot.backend.application.events.delivery.dispatcher.outbound.AbstractProgramOutboundDeliveryDispatcher;
import com.egorgoncharov.krot.backend.application.guard.view.ProgramViewGuard;
import com.egorgoncharov.krot.backend.application.guard.view.ViewGuard;
import com.egorgoncharov.krot.backend.connection.ApplicationConnectionRegistry;
import com.egorgoncharov.krot.backend.connection.ConnectionRegistry;
import com.egorgoncharov.krot.backend.database.relational.entity.ProgramEntity;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@Startup
@ApplicationScoped
public class WebSocketProgramOutboundDeliveryDispatcher extends AbstractProgramOutboundDeliveryDispatcher implements WebSocketOutboundDeliveryResolver<ProgramEntity, UUID, Void> {
    private final ApplicationConnectionRegistry connectionRegistry;
    private final WebSocketEventStreamPublisher publisher;
    private final ProgramViewGuard viewGuard;
    private final ProgramDeliveryManager deliveryManager;

    @Inject
    public WebSocketProgramOutboundDeliveryDispatcher(ApplicationConnectionRegistry connectionRegistry, WebSocketEventStreamPublisher publisher, ProgramViewGuard viewGuard, ProgramDeliveryManager deliveryManager) {
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
    protected ViewGuard<ProgramEntity, UUID, Void> viewGuard() {
        return viewGuard;
    }

    @Override
    protected Program entityReflectionConverter() {
        return new Program();
    }

    @Override
    protected AbstractDeliveryManager<ProgramEntity, UUID, Void> deliveryManager() {
        return deliveryManager;
    }

    @PostConstruct
    @Override
    protected void start() {
        super.start();
    }
}
