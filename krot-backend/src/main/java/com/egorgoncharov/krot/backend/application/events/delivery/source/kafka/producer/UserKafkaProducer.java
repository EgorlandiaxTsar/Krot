package com.egorgoncharov.krot.backend.application.events.delivery.source.kafka.producer;

import com.egorgoncharov.krot.backend.application.events.bus.AbstractEventBus;
import com.egorgoncharov.krot.backend.application.events.bus.UserEventBus;
import com.egorgoncharov.krot.backend.application.events.delivery.source.kafka.KafkaEvent;
import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import java.util.UUID;

@Startup
@ApplicationScoped
public class UserKafkaProducer extends KafkaProducer<UserEntity, UUID, Void> {
    private final UserEventBus events;

    @Inject
    @Channel("krot.out.user")
    Emitter<KafkaEvent<UUID>> emitter;

    @Inject
    public UserKafkaProducer(UserEventBus events) {
        this.events = events;
    }

    @Override
    protected AbstractEventBus<UserEntity, UUID, Void> events() {
        return events;
    }

    @Override
    protected Emitter<KafkaEvent<UUID>> emitter() {
        return emitter;
    }

    @PostConstruct
    void initBean() {
        super.start();
    }
}
