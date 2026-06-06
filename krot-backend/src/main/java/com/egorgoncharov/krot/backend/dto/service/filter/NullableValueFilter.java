package com.egorgoncharov.krot.backend.dto.service.filter;

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
