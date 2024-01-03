package com.comiclub.web.contoller.mywork.dto.edit;

import com.comiclub.web.contoller.mywork.dto.EpisodeInfo;
import com.comiclub.domain.entity.sereis.Series;
import lombok.Data;
import org.thymeleaf.util.StringUtils;

import java.util.Arrays;
import java.util.List;


@Data
public class SeriesEditDto {

    private Long id;

    private Boolean adultOnly;

    private String name;

    private String description;

    private String thumbnailUrl;

    private Integer totalEp;

    private List<String> tag;

    private List<EpisodeInfo> episodes;

    public SeriesEditDto(Series series) {
        this.id = series.getId();
        this.adultOnly = series.getAdultOnly();
        this.name = series.getName();
        this.description = series.getDescription();
        this.thumbnailUrl = series.getThumbnailUrl();
        this.totalEp = series.getTotalEp();
        this.tag = splitTag(series.getTag());
    }


    public List<String> splitTag(String tags) {
        return StringUtils.isEmpty(tags) ? null : Arrays.stream(tags.split("#")).toList();
    }
}
