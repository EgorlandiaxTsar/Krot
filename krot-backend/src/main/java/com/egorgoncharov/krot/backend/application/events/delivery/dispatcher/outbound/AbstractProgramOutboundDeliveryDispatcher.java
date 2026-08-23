package com.egorgoncharov.krot.backend.application.events.delivery.dispatcher.outbound;

import com.egorgoncharov.krot.backend.api.model.response.program.Program;
import com.egorgoncharov.krot.backend.database.relational.entity.ProgramEntity;

import java.util.UUID;

public abstract class AbstractProgramOutboundDeliveryDispatcher extends AbstractOutboundDeliveryDispatcher<ProgramEntity, UUID, Void, Program> {
}
