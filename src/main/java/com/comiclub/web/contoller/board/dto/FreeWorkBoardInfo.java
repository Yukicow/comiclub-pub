package com.comiclub.web.contoller.board.dto;

import com.comiclub.domain.entity.board.EpisodeBoard;
import com.comiclub.domain.entity.board.FreeWorkBoard;
import lombok.Data;

@Data
public class FreeWorkBoardInfo {

    private Long id;
    private String thumbnailUrl;
    private String title;
    private boolean adultOnly;

    public FreeWorkBoardInfo(FreeWorkBoard board) {
        this.id = board.getId();
        this.thumbnailUrl = board.getThumbnailUrl();
        this.title = board.getTitle();
        this.adultOnly = board.getAdultOnly();
    }




}
