package com.comiclub.web.contoller.member;


import com.comiclub.domain.entity.sereis.Series;
import lombok.Data;

@Data
public class FollowSeriesInfo {

    private Long id;
    private String name;
    private String writer;
    private String thumbnailUrl;
    private boolean adultOnly;
    private int recentUploadedEpisode;

    public FollowSeriesInfo(Series series) {
        this.id = series.getId();
        this.name = series.getName();
        this.writer = series.getWriter();
        this.thumbnailUrl = series.getThumbnailUrl();
        this.adultOnly = series.getAdultOnly();
        this.recentUploadedEpisode = series.getTotalEpBoard();
    }
}
