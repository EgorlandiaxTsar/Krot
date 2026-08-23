package com.egorgoncharov.krot.backend.application.events.delivery.dispatcher.outbound;

import com.egorgoncharov.krot.backend.api.model.response.role.Role;
import com.egorgoncharov.krot.backend.database.relational.entity.RoleEntity;

import java.util.UUID;

public abstract class AbstractRoleOutboundDeliveryDispatcher extends AbstractOutboundDeliveryDispatcher<RoleEntity, UUID, Void, Role> {
}
