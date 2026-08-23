package com.egorgoncharov.krot.backend.application.events.delivery;

import com.egorgoncharov.krot.backend.database.relational.entity.ProgramEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class ProgramDeliveryManager extends AbstractDeliveryManager<ProgramEntity, UUID, Void> {
}
