package com.comiclub.web.util.converter.formatter;

import com.comiclub.web.contoller.club.FreeBoardSearchType;
import org.springframework.format.Formatter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.service.invoker.AbstractNamedValueArgumentResolver;

import java.text.ParseException;
import java.util.Locale;

public class FreeBoardSearchTypeFormatter implements Formatter<FreeBoardSearchType> {
    @Override
    public FreeBoardSearchType parse(String text, Locale locale) throws ParseException {
        switch (text){
            case "title":
                return FreeBoardSearchType.TITLE;
            case "content":
                return FreeBoardSearchType.CONTENT;
            case "writer":
                return FreeBoardSearchType.WRITER;
            case "comment":
                return FreeBoardSearchType.COMMENT;
            default:
                return FreeBoardSearchType.NONE;
        }
    }

    @Override
    public String print(FreeBoardSearchType object, Locale locale) {
        switch (object){
            case TITLE:
                return "제목";
            case CONTENT:
                return "내용";
            case WRITER:
                return "작성자";
            case COMMENT:
                return "댓글";
            default:
                return "";
        }
    }
}
