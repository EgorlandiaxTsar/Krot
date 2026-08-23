package com.egorgoncharov.krot.backend.application.events.delivery.source.kafka.consumer;

import com.egorgoncharov.krot.backend.application.events.delivery.AbstractDeliveryManager;
import com.egorgoncharov.krot.backend.application.events.delivery.ProgramDeliveryManager;
import com.egorgoncharov.krot.backend.application.events.delivery.source.kafka.KafkaEvent;
import com.egorgoncharov.krot.backend.database.relational.RelationalCrudRepository;
import com.egorgoncharov.krot.backend.database.relational.entity.ProgramEntity;
import com.egorgoncharov.krot.backend.database.relational.repository.ProgramRepository;
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
public class ProgramKafkaConsumer extends KafkaConsumer<ProgramEntity, UUID, Void> {
    private final ProgramDeliveryManager deliveryManager;
    private final ProgramRepository repository;
    private final UserRepository principalRepository;

    @Inject
    public ProgramKafkaConsumer(ProgramDeliveryManager deliveryManager, ProgramRepository repository, UserRepository principalRepository) {
        this.deliveryManager = deliveryManager;
        this.repository = repository;
        this.principalRepository = principalRepository;
    }

    @Override
    protected AbstractDeliveryManager<ProgramEntity, UUID, Void> deliveryManager() {
        return deliveryManager;
    }

    @Override
    protected RelationalCrudRepository<ProgramEntity, UUID> repository() {
        return repository;
    }

    @Override
    protected UserRepository principalRepository() {
        return principalRepository;
    }

    @Incoming("krot.in.program")
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
