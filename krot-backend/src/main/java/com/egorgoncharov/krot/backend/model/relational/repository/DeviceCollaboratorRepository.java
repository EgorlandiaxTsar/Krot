package com.egorgoncharov.krot.backend.model.relational.repository;

import com.egorgoncharov.krot.backend.model.relational.RelationalReactiveRepository;
import com.egorgoncharov.krot.backend.model.relational.entity.DeviceCollaboratorEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class DeviceCollaboratorRepository implements RelationalReactiveRepository<DeviceCollaboratorEntity, UUID> {
}
