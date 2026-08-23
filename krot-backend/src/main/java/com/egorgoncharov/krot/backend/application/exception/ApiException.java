package com.egorgoncharov.krot.backend.application.exception;

import com.egorgoncharov.krot.backend.Result;

public class ApiException extends RuntimeException {
    public ApiException(Result<?> result) {
        super(result.getMessage());
    }
}
