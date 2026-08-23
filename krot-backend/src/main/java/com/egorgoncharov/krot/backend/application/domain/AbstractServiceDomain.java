package com.egorgoncharov.krot.backend.application.domain;

import com.egorgoncharov.krot.backend.application.service.modification.ModificationService;
import com.egorgoncharov.krot.backend.database.Identifiable;

public abstract class AbstractServiceDomain<T extends Identifiable<I>, I, A> {
    protected abstract ModificationService<T, I, A> service();
}
