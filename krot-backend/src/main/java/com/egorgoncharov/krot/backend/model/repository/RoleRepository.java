package com.egorgoncharov.krot.backend.model.repository;

import com.egorgoncharov.krot.backend.model.common.NameableEntityRepository;
import com.egorgoncharov.krot.backend.model.entity.RoleEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class RoleRepository implements NameableEntityRepository<RoleEntity, UUID> {
}