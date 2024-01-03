package com.comiclub.web.contoller.mywork.dto;


import com.comiclub.domain.entity.freework.FreeWork;
import lombok.Data;

@Data
public class FreeWorkInfo {

    private Long id;
    private String title;
    private String thumbnailUrl;

    public FreeWorkInfo(FreeWork freeWork) {
        this.id = freeWork.getId();
        this.title = freeWork.getTitle();
        this.thumbnailUrl = freeWork.getThumbnailUrl();
    }

}
