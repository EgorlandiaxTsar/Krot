package com.egorgoncharov.krot.backend.api.rest.request;

public interface EntityReflection<T> {
    T to();

    EntityReflection<T> from(T o);
}
