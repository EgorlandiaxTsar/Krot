package com.egorgoncharov.krot.backend.database;

public interface Identifiable<T> {
    T getId();

    void setId(T id);
}
