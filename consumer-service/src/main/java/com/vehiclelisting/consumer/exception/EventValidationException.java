package com.vehiclelisting.consumer.exception;

public class EventValidationException extends RuntimeException {

    public EventValidationException(String message) {
        super(message);
    }
}
