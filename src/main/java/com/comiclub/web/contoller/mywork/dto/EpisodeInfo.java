package com.comiclub.web.contoller.mywork.dto;


import com.comiclub.domain.entity.freework.FreeWork;
import com.comiclub.domain.entity.sereis.Episode;
import lombok.Data;

@Data
public class EpisodeInfo {

    private Long id;
    private String title;
    private Integer episodeNumber;
    private String thumbnailUrl;

    public EpisodeInfo(Episode episode) {
        this.id = episode.getId();
        this.title = episode.getTitle();
        this.episodeNumber = episode.getEpisodeNumber();
        this.thumbnailUrl = episode.getThumbnailUrl();
    }

}
