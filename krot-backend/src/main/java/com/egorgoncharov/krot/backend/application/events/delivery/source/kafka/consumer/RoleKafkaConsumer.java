package com.egorgoncharov.krot.backend.application.events.delivery.source.kafka.consumer;

import com.egorgoncharov.krot.backend.application.events.delivery.AbstractDeliveryManager;
import com.egorgoncharov.krot.backend.application.events.delivery.RoleDeliveryManager;
import com.egorgoncharov.krot.backend.application.events.delivery.source.kafka.KafkaEvent;
import com.egorgoncharov.krot.backend.database.relational.RelationalCrudRepository;
import com.egorgoncharov.krot.backend.database.relational.entity.RoleEntity;
import com.egorgoncharov.krot.backend.database.relational.repository.RoleRepository;
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
public class RoleKafkaConsumer extends KafkaConsumer<RoleEntity, UUID, Void> {
    private final RoleDeliveryManager deliveryManager;
    private final RoleRepository repository;
    private final UserRepository principalRepository;

    @Inject
    public RoleKafkaConsumer(RoleDeliveryManager deliveryManager, RoleRepository repository, UserRepository principalRepository) {
        this.deliveryManager = deliveryManager;
        this.repository = repository;
        this.principalRepository = principalRepository;
    }

    @Override
    protected AbstractDeliveryManager<RoleEntity, UUID, Void> deliveryManager() {
        return deliveryManager;
    }

    @Override
    protected RelationalCrudRepository<RoleEntity, UUID> repository() {
        return repository;
    }

    @Override
    protected UserRepository principalRepository() {
        return principalRepository;
    }

    @Incoming("krot.in.role")
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
