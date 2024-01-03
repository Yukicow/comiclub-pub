package com.comiclub.web.contoller.board.dto;

import com.comiclub.domain.entity.freework.FreeWorkLayer;
import com.comiclub.domain.entity.sereis.EpisodeLayer;
import lombok.Data;

@Data
public class ViewerLayerDto {

    private Integer layerNumber;
    private String imgFileUrl;
    private Double duration;

    public ViewerLayerDto(FreeWorkLayer layer) {
        this.layerNumber = layer.getLayerNumber();
        this.imgFileUrl = layer.getImgFileUrl();
        this.duration = layer.getDuration();
    }

    public ViewerLayerDto(EpisodeLayer layer) {
        this.layerNumber = layer.getLayerNumber();
        this.imgFileUrl = layer.getImgFileUrl();
        this.duration = layer.getDuration();
    }
}
