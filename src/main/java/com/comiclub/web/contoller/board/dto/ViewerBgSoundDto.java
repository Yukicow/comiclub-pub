package com.comiclub.web.contoller.board.dto;

import com.comiclub.domain.entity.freework.FWBackgroundSound;
import com.comiclub.domain.entity.sereis.EpBackgroundSound;
import lombok.Data;

@Data
public class ViewerBgSoundDto {

    private String audioFileUrl;
    private Integer startSceneNumber;
    private Integer endSceneNumber;


    public ViewerBgSoundDto(FWBackgroundSound backgroundSound) {
        this.audioFileUrl = backgroundSound.getFileUrl();
        this.startSceneNumber = backgroundSound.getStartSceneNumber();
        this.endSceneNumber = backgroundSound.getEndSceneNumber();
    }

    public ViewerBgSoundDto(EpBackgroundSound backgroundSound) {
        this.audioFileUrl = backgroundSound.getFileUrl();
        this.startSceneNumber = backgroundSound.getStartSceneNumber();
        this.endSceneNumber = backgroundSound.getEndSceneNumber();
    }
}
