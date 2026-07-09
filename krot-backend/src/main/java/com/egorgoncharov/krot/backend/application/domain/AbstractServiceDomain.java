package com.egorgoncharov.krot.backend.application.domain;

import com.egorgoncharov.krot.backend.application.service.ModifyingService;
import com.egorgoncharov.krot.backend.database.Identifiable;

public abstract class AbstractServiceDomain<T extends Identifiable<I>, I, A> {
    protected abstract ModifyingService<T, I, A> service();
}
