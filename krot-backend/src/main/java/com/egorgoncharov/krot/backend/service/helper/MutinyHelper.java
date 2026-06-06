package com.egorgoncharov.krot.backend.service.helper;

import com.egorgoncharov.krot.backend.dto.service.Result;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Optional;

public class MutinyHelper {
    public static <T> Uni<Result<T>> singletonUni(Uni<Result<List<T>>> collection) {
        return collection.map(e -> new Result<>(Optional.ofNullable(e.getResult().isPresent() ? e.getResult().get().stream().findAny().orElse(null) : null), e.getMessage(), e.getCode(), e.getTimestamp()));
    }
}
