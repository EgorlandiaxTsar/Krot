package com.egorgoncharov.krot.backend.application.events.delivery.dispatcher.outbound;

import com.egorgoncharov.krot.backend.api.model.response.device.Device;
import com.egorgoncharov.krot.backend.database.relational.entity.DeviceEntity;

import java.util.UUID;

public abstract class AbstractDeviceOutboundDeliveryDispatcher extends AbstractOutboundDeliveryDispatcher<DeviceEntity, UUID, Void, Device> {
}
