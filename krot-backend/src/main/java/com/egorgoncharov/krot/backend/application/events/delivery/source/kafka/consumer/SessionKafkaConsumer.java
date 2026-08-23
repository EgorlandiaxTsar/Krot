package com.egorgoncharov.krot.backend.application.events.delivery.source.kafka.consumer;

import com.egorgoncharov.krot.backend.application.events.delivery.AbstractDeliveryManager;
import com.egorgoncharov.krot.backend.application.events.delivery.SessionDeliveryManager;
import com.egorgoncharov.krot.backend.application.events.delivery.source.kafka.KafkaEvent;
import com.egorgoncharov.krot.backend.database.relational.RelationalCrudRepository;
import com.egorgoncharov.krot.backend.database.relational.entity.HistoricalSessionEntity;
import com.egorgoncharov.krot.backend.database.relational.repository.HistoricalSessionRepository;
import com.egorgoncharov.krot.backend.database.relational.repository.UserRepository;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.util.UUID;

@Startup
@ApplicationScoped
public class SessionKafkaConsumer extends KafkaConsumer<HistoricalSessionEntity, UUID, Void> {
    private final SessionDeliveryManager deliveryManager;
    private final HistoricalSessionRepository repository;
    private final UserRepository principalRepository;

    @Inject
    public SessionKafkaConsumer(SessionDeliveryManager deliveryManager, HistoricalSessionRepository repository, UserRepository principalRepository) {
        this.deliveryManager = deliveryManager;
        this.repository = repository;
        this.principalRepository = principalRepository;
    }

    @Override
    protected AbstractDeliveryManager<HistoricalSessionEntity, UUID, Void> deliveryManager() {
        return deliveryManager;
    }

    @Override
    protected RelationalCrudRepository<HistoricalSessionEntity, UUID> repository() {
        return repository;
    }

    @Override
    protected UserRepository principalRepository() {
        return principalRepository;
    }

    @Incoming("krot.in.session")
    @Override
    protected Uni<Void> consume(KafkaEvent<UUID> event) {
        return super.consume(event);
    }

    @PostConstruct
    @Override
    protected void start() {
        super.start();
    }
}
