package com.egorgoncharov.krot.backend.application.events.bus;

import com.egorgoncharov.krot.backend.database.relational.entity.ProgramEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class ProgramEventBus extends AbstractEventBus<ProgramEntity, UUID, Void> {
}
