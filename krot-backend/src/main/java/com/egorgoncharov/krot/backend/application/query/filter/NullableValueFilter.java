package com.egorgoncharov.krot.backend.application.query.filter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class NullableValueFilter<T> {
    private boolean requirePresence;
    private T filter;
}
