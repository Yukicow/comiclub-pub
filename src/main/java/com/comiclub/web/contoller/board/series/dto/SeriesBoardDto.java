package com.comiclub.web.contoller.board.series.dto;


import com.comiclub.domain.entity.sereis.Series;
import lombok.Data;
import org.thymeleaf.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Data
public class SeriesBoardDto {

    private Long id;
    private Boolean adultOnly;
    private String name;
    private String description;
    private String writer;
    private String thumbnailUrl;
    private Integer totalFollower;
    private Integer totalLike;
    private Long totalView;
    private Integer totalEpBoard;
    private List<String> tag;

    public SeriesBoardDto(Series series) {
        this.id = series.getId();
        this.adultOnly = series.getAdultOnly();
        this.name = series.getName();
        this.description = series.getDescription();
        this.writer = series.getWriter();
        this.thumbnailUrl = series.getThumbnailUrl();
        this.totalFollower = series.getTotalFollower();
        this.totalLike = series.getTotalLike();
        this.totalView = series.getTotalView();
        this.totalEpBoard = series.getTotalEpBoard();
        this.tag = splitTag(series.getTag());
    }



    public List<String> splitTag(String tags) {
        return StringUtils.isEmpty(tags) ? null : Arrays.stream(tags.split("#")).toList();
    }
}
