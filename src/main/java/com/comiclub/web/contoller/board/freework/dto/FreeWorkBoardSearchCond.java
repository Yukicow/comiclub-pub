package com.comiclub.web.contoller.board.freework.dto;


import com.comiclub.web.contoller.board.OrderType;
import com.comiclub.web.contoller.board.freework.FreeWorkSearchType;
import lombok.Data;

@Data
public class FreeWorkBoardSearchCond {

    private FreeWorkSearchType type = FreeWorkSearchType.NONE;

    private String keyword;

    private OrderType order = OrderType.LATEST;

    private Boolean viewAdult = false; // 기본은 false -> 로그인한 유저가 있고 viewAdult가 true이면 true로 set
}
