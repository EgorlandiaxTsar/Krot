package com.egorgoncharov.krot.backend.application.exception;

public class EventRecompositionException extends RuntimeException {
    public EventRecompositionException(String message, Throwable t) {
        super(message, t);
    }

    public EventRecompositionException(String message) {
        super(message);
    }
}
