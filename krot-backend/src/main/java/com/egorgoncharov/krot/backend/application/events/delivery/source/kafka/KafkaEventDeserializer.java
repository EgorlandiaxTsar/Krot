package com.egorgoncharov.krot.backend.application.events.delivery.source.kafka;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class KafkaEventDeserializer extends ObjectMapperDeserializer<KafkaEvent> {
    public KafkaEventDeserializer() {
        super(KafkaEvent.class);
    }
}
