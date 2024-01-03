package com.comiclub.web.util.converter.formatter;

import com.comiclub.domain.entity.board.enumtype.Topic;
import org.springframework.format.Formatter;

import java.text.ParseException;
import java.util.Locale;

public class TopicFormatter implements Formatter<Topic> {
    @Override
    public Topic parse(String text, Locale locale) throws ParseException {
        switch (text){
            case "notice":
            case "NOTICE":
                return Topic.NOTICE;
            case "best":
            case "BEST":
                return Topic.BEST;
            case "free":
            case "FREE":
                return Topic.FREE;
            case "work":
            case "WORK":
                return Topic.WORK;
            case "question":
            case "QUESTION":
                return Topic.QUESTION;
            default:
                return Topic.ALL;
        }

    }


    @Override
    public String print(Topic object, Locale locale) {
        switch (object){
            case NOTICE:
                return "공지";
            case BEST:
                return "인기";
            case FREE:
                return "자유";
            case WORK:
                return "작품";
            case QUESTION:
                return "질문";
            default:
                return "기타";
        }
    }
}
