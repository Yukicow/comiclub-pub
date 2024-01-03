package com.comiclub.web.contoller.board.series.dto;


import com.comiclub.domain.entity.freework.FreeWork;
import com.comiclub.domain.entity.sereis.Episode;
import com.comiclub.web.contoller.board.dto.ViewerBgSoundDto;
import com.comiclub.web.contoller.board.dto.ViewerSceneDto;
import com.comiclub.web.contoller.mywork.dto.SceneDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ViewerEpisodeDto {

    private Integer episodeNumber;
    private boolean freeUse;
    private boolean autoMode;

    private List<ViewerSceneDto> scenes;
    private List<ViewerBgSoundDto> bgSounds;

    public ViewerEpisodeDto(Episode episode) {
        List<ViewerSceneDto> scenes = new ArrayList<>();
        episode.getScenes().forEach(scene -> {
            ViewerSceneDto viewerSceneDto = new ViewerSceneDto(scene);
            scenes.add(viewerSceneDto);
        });

        List<ViewerBgSoundDto> bgSounds = new ArrayList<>();
        episode.getBgSounds().forEach(bgSound -> {
            ViewerBgSoundDto viewerBgSoundDto = new ViewerBgSoundDto(bgSound);
            bgSounds.add(viewerBgSoundDto);
        });
        this.episodeNumber = episode.getEpisodeNumber();
        this.freeUse = episode.getFreeUse();
        this.autoMode = episode.getAutoMode();
        this.scenes = scenes;
        this.bgSounds = bgSounds;
    }


}
