package com.comiclub.web.result;


import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ErrorResult {

    private int status;
    private String code;
    private String message;

    public ErrorResult(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public ErrorResult() {
    }
}
