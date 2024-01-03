package com.comiclub.web.util.converter.formatter;

import com.comiclub.web.contoller.board.series.SeriesSearchType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.Formatter;

import java.text.ParseException;
import java.util.Locale;

@Slf4j
public class SeriesSearchTypeFormatter implements Formatter<SeriesSearchType> {
    @Override
    public SeriesSearchType parse(String text, Locale locale) throws ParseException {
        switch (text){
            case "name":
                return SeriesSearchType.NAME;
            case "description":
                return SeriesSearchType.DESCRIPTION;
            case "writer":
                return SeriesSearchType.WRITER;
            default:
                return SeriesSearchType.NONE;
        }
    }

    @Override
    public String print(SeriesSearchType object, Locale locale) {
        switch (object){
            case NAME:
                return "작품명";
            case DESCRIPTION:
                return "줄거리";
            case WRITER:
                return "작성자";
            default:
                return "";
        }
    }
}
