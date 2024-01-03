package com.comiclub.web.contoller.board;

import lombok.Getter;

@Getter
public enum OrderType {

    LATEST("latest"), BEST("best");

    private String value;

    OrderType(String value) {
        this.value = value;
    }
}
