package com.comiclub.web.exception;

public class NoSuchLoginInfoException extends RuntimeException{

    public NoSuchLoginInfoException() {
        super();
    }

    public NoSuchLoginInfoException(String message) {
        super(message);
    }

    public NoSuchLoginInfoException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchLoginInfoException(Throwable cause) {
        super(cause);
    }
}
