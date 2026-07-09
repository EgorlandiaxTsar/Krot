package com.egorgoncharov.krot.backend.application.context;

import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;

public interface PrincipalContext {
    UserEntity principal();
}
