package com.egorgoncharov.krot.backend.application.context;

import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public abstract class AbstractApplicationContext<T> implements AdditionalContext<T>, PrincipalContext {
    private final UserEntity principal;
    private final T additionalContext;

    @Override
    public T additionalContext() {
        return additionalContext;
    }

    @Override
    public UserEntity principal() {
        return principal;
    }
}
