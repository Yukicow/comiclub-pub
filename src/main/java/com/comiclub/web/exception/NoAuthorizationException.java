package com.comiclub.web.exception;

public class NoAuthorizationException extends RuntimeException {

    public NoAuthorizationException() {
        super();
    }

    public NoAuthorizationException(String message) {
        super(message);
    }

    public NoAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoAuthorizationException(Throwable cause) {
        super(cause);
    }
}
