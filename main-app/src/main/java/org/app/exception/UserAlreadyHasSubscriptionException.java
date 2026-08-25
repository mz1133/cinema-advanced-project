package org.app.exception;

public class UserAlreadyHasSubscriptionException extends RuntimeException {

    public UserAlreadyHasSubscriptionException(String message) {
        super(message);
    }
}
