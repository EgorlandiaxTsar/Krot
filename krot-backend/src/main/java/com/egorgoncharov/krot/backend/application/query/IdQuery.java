package com.egorgoncharov.krot.backend.application.query;

import java.util.List;

public interface IdQuery<T> {
    List<T> getIds();
}
