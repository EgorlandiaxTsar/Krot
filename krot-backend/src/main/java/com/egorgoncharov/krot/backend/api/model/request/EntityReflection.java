package com.egorgoncharov.krot.backend.api.model.request;

public interface EntityReflection<T> {
    T to();

    EntityReflection<T> from(T o);
}
