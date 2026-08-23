package com.egorgoncharov.krot.backend.application.events.delivery;

import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class UserDeliveryManager extends AbstractDeliveryManager<UserEntity, UUID, Void> {
}
