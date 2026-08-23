package com.egorgoncharov.krot.backend.application.context;

import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public abstract class AbstractApplicationContext<A> implements AdditionalContext<A>, PrincipalContext {
    private final UserEntity principal;
    private final A additionalContext;

    @Override
    public A additionalContext() {
        return additionalContext;
    }

    @Override
    public UserEntity principal() {
        return principal;
    }
}
