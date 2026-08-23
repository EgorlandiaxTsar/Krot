package com.egorgoncharov.krot.backend.util;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.application.exception.ApiException;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Streams {
    public static <T> Uni<Result<T>> singletonUni(Uni<Result<List<T>>> collection) {
        return collection.map(e -> new Result<>(Optional.ofNullable(e.getResult().isPresent() ? e.getResult().get().stream().findAny().orElse(null) : null), e.getMessage(), e.getCode(), e.getTimestamp()));
    }

    public static <T> Uni<ApiException> hasError(Result<T> result, Function<Result<T>, Boolean> errorFn) {
        return errorFn.apply(result) ? Uni.createFrom().failure(new ApiException(result)) : null;
    }
}
