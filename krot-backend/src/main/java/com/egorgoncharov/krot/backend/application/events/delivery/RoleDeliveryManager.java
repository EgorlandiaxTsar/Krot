package com.egorgoncharov.krot.backend.application.events.delivery;

import com.egorgoncharov.krot.backend.database.relational.entity.RoleEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class RoleDeliveryManager extends AbstractDeliveryManager<RoleEntity, UUID, Void> {
}
