package com.comiclub.web.contoller.common.dto;


import lombok.Data;

@Data
public class CommentSaveDto {

    private Long memberId;

    private Long boardId;

    private Long motherCommentId;

    private String writer;

    private String content;

    public CommentSaveDto(Long memberId, Long boardId, Long motherCommentId, String writer, String content) {
        this.memberId = memberId;
        this.boardId = boardId;
        this.motherCommentId = motherCommentId;
        this.writer = writer;
        this.content = content;
    }
}
