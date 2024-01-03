package com.comiclub.web.util.converter.formatter;

import com.comiclub.web.contoller.board.OrderType;
import com.comiclub.web.contoller.club.FreeBoardSearchType;
import org.springframework.format.Formatter;

import java.text.ParseException;
import java.util.Locale;

public class OrderTypeFormatter implements Formatter<OrderType> {
    @Override
    public OrderType parse(String text, Locale locale) throws ParseException {
        switch (text){
            case "best":
                return OrderType.BEST;
            default:
                return OrderType.LATEST;
        }
    }

    @Override
    public String print(OrderType object, Locale locale) {
        switch (object){
            case BEST:
                return "추천순";
            default:
                return "최신순";
        }
    }
}
