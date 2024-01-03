package com.comiclub.web.contoller.common.dto;


import com.comiclub.domain.entity.board.EpisodeBoardComment;
import com.comiclub.domain.entity.board.FreeBoardComment;
import com.comiclub.domain.entity.board.FreeWorkBoardComment;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Data
public class CommentDto {

    private Long id;
    private String writer;
    private String content;

    private String createdDate;
    private Integer totalLike;
    private Integer totalUnlike;
    private Integer totalReply;

    public CommentDto(FreeBoardComment comment) {
        this.id = comment.getId();
        this.writer = comment.getWriter();
        this.content = comment.getContent();
        insertDate(comment.getCreatedDate());
        this.totalLike = comment.getTotalLike();
        this.totalUnlike = comment.getTotalUnlike();
        this.totalReply = comment.getTotalReply();
    }

    public CommentDto(FreeWorkBoardComment comment) {
        this.id = comment.getId();
        this.writer = comment.getWriter();
        this.content = comment.getContent();
        insertDate(comment.getCreatedDate());
        this.totalLike = comment.getTotalLike();
        this.totalUnlike = comment.getTotalUnlike();
        this.totalReply = comment.getTotalReply();
    }

    public CommentDto(EpisodeBoardComment comment) {
        this.id = comment.getId();
        this.writer = comment.getWriter();
        this.content = comment.getContent();
        insertDate(comment.getCreatedDate());
        this.totalLike = comment.getTotalLike();
        this.totalUnlike = comment.getTotalUnlike();
        this.totalReply = comment.getTotalReply();
    }


    public void insertDate(LocalDateTime object) {
        LocalDateTime today = LocalDateTime.now();
        boolean isSameDay =
                ( object.getYear() == today.getYear() )
                        && ( object.getMonth() == today.getMonth() )
                        && ( object.getDayOfMonth() == today.getDayOfMonth() );
        if(isSameDay) this.createdDate = object.format(DateTimeFormatter.ofPattern("HH:mm"));
        else this.createdDate = object.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

}
