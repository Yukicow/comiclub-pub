package com.comiclub.web.contoller.board.freework.dto;

import com.comiclub.domain.entity.freework.FreeWork;
import com.comiclub.web.contoller.board.dto.ViewerBgSoundDto;
import com.comiclub.web.contoller.board.dto.ViewerSceneDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ViewerFreeWorkDto {

    private boolean freeUse;
    private boolean autoMode;
    private List<ViewerSceneDto> scenes;
    private List<ViewerBgSoundDto> bgSounds;
    public ViewerFreeWorkDto(FreeWork freeWork) {
        List<ViewerSceneDto> scenes = new ArrayList<>();
        freeWork.getScenes().forEach(scene -> {
            ViewerSceneDto viewerSceneDto = new ViewerSceneDto(scene);
            scenes.add(viewerSceneDto);
        });

        List<ViewerBgSoundDto> bgSounds = new ArrayList<>();
        freeWork.getBgSounds().forEach(bgSound -> {
            ViewerBgSoundDto viewerBgSoundDto = new ViewerBgSoundDto(bgSound);
            bgSounds.add(viewerBgSoundDto);
        });
        this.freeUse = freeWork.getFreeUse();
        this.autoMode = freeWork.getAutoMode();
        this.scenes = scenes;
        this.bgSounds = bgSounds;
    }
}
