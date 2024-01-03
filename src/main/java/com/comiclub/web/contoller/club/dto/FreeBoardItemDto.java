package com.comiclub.web.contoller.club.dto;

import com.comiclub.domain.entity.board.FreeBoard;
import com.comiclub.domain.entity.board.enumtype.Topic;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
public class FreeBoardItemDto {
    private Long id;
    private Topic topic;
    private String title;
    private Boolean adultOnly;
    private String writer;
    private Long totalView;
    private Integer totalLike;
    private Integer totalComments;
    private LocalDateTime createdDate;

    public FreeBoardItemDto(FreeBoard board) {
        this.id = board.getId();
        this.topic = board.getTopic();
        this.title = board.getTitle();
        this.adultOnly = board.getAdultOnly();
        this.writer = board.getWriter();
        this.totalView = board.getTotalView();
        this.totalLike = board.getTotalLike();
        this.totalComments = board.getTotalComment();
        this.createdDate = board.getCreatedDate();
    }
}
