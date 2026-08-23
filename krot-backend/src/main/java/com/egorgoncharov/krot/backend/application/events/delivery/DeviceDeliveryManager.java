package com.egorgoncharov.krot.backend.application.events.delivery;

import com.egorgoncharov.krot.backend.database.relational.entity.DeviceEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class DeviceDeliveryManager extends AbstractDeliveryManager<DeviceEntity, UUID, Void> {
}
