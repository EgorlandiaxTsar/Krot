package com.egorgoncharov.krot.backend.model.relational.repository;

import com.egorgoncharov.krot.backend.model.relational.RelationalNameableRepository;
import com.egorgoncharov.krot.backend.model.relational.entity.RoleEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class RoleRepository implements RelationalNameableRepository<RoleEntity, UUID> {
}