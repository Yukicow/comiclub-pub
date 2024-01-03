package com.comiclub.web.result;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class CommonResult<T> {

    private int status;
    private String code;
    private T data;

    public CommonResult() {
    }
}
