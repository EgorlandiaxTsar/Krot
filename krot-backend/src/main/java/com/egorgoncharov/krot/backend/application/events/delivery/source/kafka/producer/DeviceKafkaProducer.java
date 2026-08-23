package com.egorgoncharov.krot.backend.application.events.delivery.source.kafka.producer;

import com.egorgoncharov.krot.backend.application.events.bus.AbstractEventBus;
import com.egorgoncharov.krot.backend.application.events.bus.DeviceEventBus;
import com.egorgoncharov.krot.backend.application.events.delivery.source.kafka.KafkaEvent;
import com.egorgoncharov.krot.backend.database.relational.entity.DeviceEntity;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import java.util.UUID;

@Startup
@ApplicationScoped
public class DeviceKafkaProducer extends KafkaProducer<DeviceEntity, UUID, Void> {
    private final DeviceEventBus events;

    @Inject
    @Channel("krot.out.device")
    Emitter<KafkaEvent<UUID>> emitter;

    @Inject
    public DeviceKafkaProducer(DeviceEventBus events) {
        this.events = events;
    }

    @Override
    protected AbstractEventBus<DeviceEntity, UUID, Void> events() {
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
