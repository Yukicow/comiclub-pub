package com.comiclub.web.util.converter.formatter;

import org.springframework.format.Formatter;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TimeFormatter implements Formatter<LocalDateTime> {

    @Override
    public LocalDateTime parse(String text, Locale locale) throws ParseException {
        return LocalDateTime.parse(text);
    }

    @Override
    public String print(LocalDateTime object, Locale locale) {
        LocalDateTime today = LocalDateTime.now();
        boolean isSameDay =
                    ( object.getYear() == today.getYear() )
                && ( object.getMonth() == today.getMonth() )
                && ( object.getDayOfMonth() == today.getDayOfMonth() );
        if(isSameDay) return object.format(DateTimeFormatter.ofPattern("HH:mm"));
        else if( object.getYear() == today.getYear() ) return object.format(DateTimeFormatter.ofPattern("MM-dd"));
        else return object.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
