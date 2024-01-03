package com.comiclub.web.contoller.board.series.dto;


import com.comiclub.domain.entity.sereis.Series;
import lombok.Data;
import org.thymeleaf.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Data
public class SeriesSideDto {

    private Long id;
    private String thumbnailUrl;
    private String name;
    private List<String> tag;
    private boolean adultOnly;

    public SeriesSideDto(Series series) {
        this.id = series.getId();
        this.thumbnailUrl = series.getThumbnailUrl();
        this.name = series.getName();
        this.tag = splitTag(series.getTag());
        this.adultOnly = series.getAdultOnly();
    }

    public List<String> splitTag(String tags) {
        return StringUtils.isEmpty(tags) ? null : Arrays.stream(tags.split("#")).toList();
    }


}
