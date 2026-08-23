package com.egorgoncharov.krot.backend.application.events.delivery.dispatcher.outbound;

import com.egorgoncharov.krot.backend.api.model.response.ApiEvent;
import com.egorgoncharov.krot.backend.api.model.response.user.User;
import com.egorgoncharov.krot.backend.application.context.model.EventContext;
import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;

import java.util.UUID;

public abstract class AbstractUserOutboundDeliveryDispatcher extends AbstractOutboundDeliveryDispatcher<UserEntity, UUID, Void, User> {
    @Override
    protected ApiEvent<UserEntity, UUID, Void> transformEventContext(EventContext<UserEntity, UUID, Void> context) {
        ApiEvent<UserEntity, UUID, Void> event = super.transformEventContext(context);
        event.setTargets(event.getTargets().stream().map(user -> {
            UserEntity template = user.to();
            template.setPassword(null);
            return user.from(template);
        }).toList());
        return event;
    }
}
