package com.comiclub.web.contoller.mywork.dto.edit;


import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class EpisodeEditInfo {

    private String title;

    private Boolean autoMode = false;

    private Boolean freeUse = false;

    private Integer episodeNumber;

    private String seriesName;

    private String thumbnailUrl;
}
