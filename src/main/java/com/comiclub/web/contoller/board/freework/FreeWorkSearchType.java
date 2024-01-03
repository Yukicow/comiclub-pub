package com.comiclub.web.contoller.board.freework;

import lombok.Getter;

@Getter
public enum FreeWorkSearchType {

    NONE(""), TITLE("title"), WRITER("writer");

    private String value;

    FreeWorkSearchType(String value) {
        this.value = value;
    }
}
