package com.comiclub.web.contoller.mywork.dto;

import com.comiclub.domain.entity.freework.FWBackgroundSound;
import com.comiclub.domain.entity.sereis.EpBackgroundSound;
import lombok.Data;

@Data
public class BgSoundInfo {

    private Long id;

    private String fileUrl;

    public BgSoundInfo(FWBackgroundSound fwBackgroundSound) {
        this.id = fwBackgroundSound.getId();
        this.fileUrl = fwBackgroundSound.getFileUrl();
    }

    public BgSoundInfo(EpBackgroundSound epBackgroundSound) {
        this.id = epBackgroundSound.getId();
        this.fileUrl = epBackgroundSound.getFileUrl();
    }
}
