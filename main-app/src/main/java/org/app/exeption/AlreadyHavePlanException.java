package org.app.exeption;

public class AlreadyHavePlanException extends RuntimeException {

    public AlreadyHavePlanException(String message) {
        super(message);
    }
}
