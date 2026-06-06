package com.egorgoncharov.krot.backend.model.repository;

import com.egorgoncharov.krot.backend.model.common.NameableEntityRepository;
import com.egorgoncharov.krot.backend.model.entity.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class UserRepository implements NameableEntityRepository<UserEntity, UUID> {
    @Override
    public String sqlNameField() {
        return "username";
    }
}
