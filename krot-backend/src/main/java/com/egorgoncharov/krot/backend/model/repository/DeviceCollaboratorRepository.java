package com.egorgoncharov.krot.backend.model.repository;

import com.egorgoncharov.krot.backend.model.common.ReactiveRepository;
import com.egorgoncharov.krot.backend.model.entity.DeviceCollaboratorEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class DeviceCollaboratorRepository implements ReactiveRepository<DeviceCollaboratorEntity, UUID> {
}
