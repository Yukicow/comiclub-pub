package com.comiclub.web.contoller.board.dto;


import com.comiclub.domain.entity.board.EpisodeBoard;
import com.comiclub.domain.entity.board.FreeWorkBoard;
import lombok.Data;

import java.util.List;

@Data
public class ViewerBoardDto {

    private Long id;
    private String title;
    private String authorWords;
    private Integer totalComment;

    public ViewerBoardDto(FreeWorkBoard board) {
        this.id = board.getId();
        this.title = board.getTitle();
        this.authorWords = board.getAuthorWords();
        this.totalComment = board.getTotalComment();
    }

    public ViewerBoardDto(EpisodeBoard board) {
        this.id = board.getId();
        this.title = board.getTitle();
        this.authorWords = board.getAuthorWords();
        this.totalComment = board.getTotalComment();
    }
}
