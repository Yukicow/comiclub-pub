package com.comiclub.web.contoller.club;

import com.comiclub.domain.entity.board.enumtype.Topic;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class BoardSearchCond {

    private Topic topic = Topic.ALL; // Default는 모든 게시글 조회
    private FreeBoardSearchType type = FreeBoardSearchType.NONE; // 아무 검색도 안 하는 경우를 Default로 설정
    private String keyword;
    private Boolean viewAdult = false; // 기본은 false -> 로그인한 유저가 있고 viewAdult가 true이면 true로 set


}
