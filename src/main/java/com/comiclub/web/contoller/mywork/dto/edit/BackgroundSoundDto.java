package com.comiclub.web.contoller.mywork.dto.edit;

import com.comiclub.domain.entity.freework.FWBackgroundSound;
import com.comiclub.domain.entity.sereis.EpBackgroundSound;
import lombok.Data;


@Data
public class BackgroundSoundDto {

    private Long id;
    private String fileName;
    private String fileUrl;
    private Integer startSceneNumber;
    private Integer endSceneNumber;

    public BackgroundSoundDto(FWBackgroundSound backgroundSound) {
        this.id = backgroundSound.getId();
        this.fileName = backgroundSound.getFileName();
        this.fileUrl = backgroundSound.getFileUrl();
        this.startSceneNumber = backgroundSound.getStartSceneNumber();
        this.endSceneNumber = backgroundSound.getEndSceneNumber();
    }

    public BackgroundSoundDto(EpBackgroundSound backgroundSound) {
        this.id = backgroundSound.getId();
        this.fileName = backgroundSound.getFileName();
        this.fileUrl = backgroundSound.getFileUrl();
        this.startSceneNumber = backgroundSound.getStartSceneNumber();
        this.endSceneNumber = backgroundSound.getEndSceneNumber();
    }


}
