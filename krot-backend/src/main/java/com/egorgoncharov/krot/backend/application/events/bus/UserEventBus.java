package com.egorgoncharov.krot.backend.application.events.bus;

import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class UserEventBus extends AbstractEventBus<UserEntity, UUID, Void> {
}
