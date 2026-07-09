package com.egorgoncharov.krot.backend.database.relational.repository;

import com.egorgoncharov.krot.backend.database.relational.RelationalNameableRepository;
import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class UserRepository implements RelationalNameableRepository<UserEntity, UUID> {
    @Override
    public String sqlNameField() {
        return "username";
    }
}
