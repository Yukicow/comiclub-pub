package com.comiclub.web.contoller.mywork.dto;


import com.comiclub.domain.entity.freework.FreeWorkLayer;
import com.comiclub.domain.entity.sereis.EpisodeLayer;
import lombok.Data;

@Data
public class LayerDto {

    private Long id;

    private Double duration;

    private String imgFileUrl;

    private Integer layerNumber;

    public LayerDto(FreeWorkLayer layer) {
        this.id = layer.getId();
        this.duration = layer.getDuration();
        this.imgFileUrl = layer.getImgFileUrl();
        this.layerNumber = layer.getLayerNumber();
    }

    public LayerDto(EpisodeLayer layer) {
        this.id = layer.getId();
        this.duration = layer.getDuration();
        this.imgFileUrl = layer.getImgFileUrl();
        this.layerNumber = layer.getLayerNumber();
    }


}
