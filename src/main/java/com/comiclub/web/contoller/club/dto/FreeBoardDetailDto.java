package com.comiclub.web.contoller.club.dto;


import com.comiclub.web.contoller.common.dto.CommentDto;
import com.comiclub.domain.entity.board.FreeBoard;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class FreeBoardDetailDto {


    private Long id;
    private String title;
    private String writer;
    private String content;
    private Long totalView;
    private Integer totalLike;
    private Integer totalComment;
    private LocalDateTime createdDate;

    public FreeBoardDetailDto(FreeBoard board) {
        this.id = board.getId();
        this.title = board.getTitle();
        this.writer = board.getWriter();
        this.content = board.getContent();
        this.totalView = board.getTotalView();
        this.totalLike = board.getTotalLike();
        this.totalComment = board.getTotalComment();
        this.createdDate = board.getCreatedDate();
    }


}

