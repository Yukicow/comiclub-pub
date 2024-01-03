package com.comiclub.web.exception;

public class PrivateStateException extends RuntimeException {
    public PrivateStateException() {
        super();
    }

    public PrivateStateException(String message) {
        super(message);
    }

    public PrivateStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public PrivateStateException(Throwable cause) {
        super(cause);
    }

}
