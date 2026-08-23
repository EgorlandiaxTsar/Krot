package com.egorgoncharov.krot.backend.application.events.bus;

import com.egorgoncharov.krot.backend.database.relational.entity.DeviceEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class DeviceEventBus extends AbstractEventBus<DeviceEntity, UUID, Void> {
}
