package com.egorgoncharov.krot.backend.application.exception;

import com.egorgoncharov.krot.backend.application.Result;

public class ApplicationException extends RuntimeException {
    public ApplicationException(Result<?> result) {
        super(result.getMessage());
    }
}
