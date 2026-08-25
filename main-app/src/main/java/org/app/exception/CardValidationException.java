package org.app.exception;

public class CardValidationException extends RuntimeException {
    public CardValidationException(String message) {
        super(message);
    }
}
