package com.comiclub.web.contoller.mywork.dto.edit;


import com.comiclub.domain.entity.freework.FreeWork;
import com.comiclub.domain.entity.sereis.Episode;
import lombok.Data;

import java.util.List;

@Data
public class WorkEditDto {

    private Long id;

    private String title;

    private String thumbnailUrl;

    private Boolean autoMode;

    private List<SceneViewDto> scenes;

    private List<BackgroundSoundDto> bgSounds;

    public WorkEditDto(FreeWork freeWork, List<SceneViewDto> scenes, List<BackgroundSoundDto> bgSounds) {
        this.id = freeWork.getId();
        this.title = freeWork.getTitle();
        this.thumbnailUrl = freeWork.getThumbnailUrl();
        this.autoMode = freeWork.getAutoMode();
        this.scenes = scenes;
        this.bgSounds = bgSounds;
    }
    public WorkEditDto(Episode episode, List<SceneViewDto> scenes, List<BackgroundSoundDto> bgSounds) {
        this.id = episode.getId();
        this.title = episode.getTitle();
        this.thumbnailUrl = episode.getThumbnailUrl();
        this.autoMode = episode.getAutoMode();
        this.scenes = scenes;
        this.bgSounds = bgSounds;
    }


}
