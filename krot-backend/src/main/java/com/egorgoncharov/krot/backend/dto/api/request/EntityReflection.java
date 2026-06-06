package com.egorgoncharov.krot.backend.dto.api.request;

public interface EntityReflection<T> {
    T to();

    EntityReflection<T> from(T o);
}
