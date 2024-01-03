package com.comiclub.web.exception;

public class AutoModeFalseException extends RuntimeException{
    public AutoModeFalseException() {
        super();
    }

    public AutoModeFalseException(String message) {
        super(message);
    }

    public AutoModeFalseException(String message, Throwable cause) {
        super(message, cause);
    }

    public AutoModeFalseException(Throwable cause) {
        super(cause);
    }
}
