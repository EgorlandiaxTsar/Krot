package com.egorgoncharov.krot.backend.application.events.delivery.source.kafka.consumer;

import com.egorgoncharov.krot.backend.application.events.delivery.AbstractDeliveryManager;
import com.egorgoncharov.krot.backend.application.events.delivery.UserDeliveryManager;
import com.egorgoncharov.krot.backend.application.events.delivery.source.kafka.KafkaEvent;
import com.egorgoncharov.krot.backend.database.relational.RelationalCrudRepository;
import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
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
public class UserKafkaConsumer extends KafkaConsumer<UserEntity, UUID, Void> {
    private final UserDeliveryManager deliveryManager;
    private final UserRepository repository;
    private final UserRepository principalRepository;

    @Inject
    public UserKafkaConsumer(UserDeliveryManager deliveryManager, UserRepository repository, UserRepository principalRepository) {
        this.deliveryManager = deliveryManager;
        this.repository = repository;
        this.principalRepository = principalRepository;
    }

    @Override
    protected AbstractDeliveryManager<UserEntity, UUID, Void> deliveryManager() {
        return deliveryManager;
    }

    @Override
    protected RelationalCrudRepository<UserEntity, UUID> repository() {
        return repository;
    }

    @Override
    protected UserRepository principalRepository() {
        return principalRepository;
    }

    @Incoming("krot.in.user")
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
