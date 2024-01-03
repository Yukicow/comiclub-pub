package com.comiclub.web.exception;

public class AdultOnlyAccessibleException extends RuntimeException{

    public AdultOnlyAccessibleException() {
        super();
    }

    public AdultOnlyAccessibleException(String message) {
        super(message);
    }

    public AdultOnlyAccessibleException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdultOnlyAccessibleException(Throwable cause) {
        super(cause);
    }
}
