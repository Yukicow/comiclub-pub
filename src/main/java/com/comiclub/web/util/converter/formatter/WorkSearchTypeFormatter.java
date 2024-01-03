package com.comiclub.web.util.converter.formatter;

import com.comiclub.web.contoller.board.freework.FreeWorkSearchType;
import org.springframework.format.Formatter;

import java.text.ParseException;
import java.util.Locale;

public class WorkSearchTypeFormatter implements Formatter<FreeWorkSearchType> {
    @Override
    public FreeWorkSearchType parse(String text, Locale locale) throws ParseException {
        switch (text){
            case "title":
                return FreeWorkSearchType.TITLE;
            case "writer":
                return FreeWorkSearchType.WRITER;
            default:
                return FreeWorkSearchType.NONE;
        }
    }

    @Override
    public String print(FreeWorkSearchType object, Locale locale) {
        switch (object){
            case TITLE:
                return "제목";
            case WRITER:
                return "작성자";
            default:
                return "";
        }
    }
}
