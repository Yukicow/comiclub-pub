package com.comiclub.web.contoller.board.dto;

import com.comiclub.domain.entity.board.EpisodeBoard;
import lombok.Data;


@Data
public class EpisodeBoardInfo {

    private Long id;
    private String thumbnailUrl;
    private Integer episodeNumber;
    private String title;
    private boolean adultOnly;

    public EpisodeBoardInfo(EpisodeBoard board) {
        this.id = board.getId();
        this.thumbnailUrl = board.getThumbnailUrl();
        this.episodeNumber = board.getEpisodeNumber();
        this.title = board.getTitle();
        this.adultOnly = board.getAdultOnly();
    }
}
