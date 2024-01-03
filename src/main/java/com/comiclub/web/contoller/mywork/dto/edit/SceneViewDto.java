package com.comiclub.web.contoller.mywork.dto.edit;


import com.comiclub.domain.entity.freework.FreeWorkScene;
import com.comiclub.domain.entity.sereis.EpisodeScene;
import lombok.Data;

@Data
public class SceneViewDto {

    private Long id;
    private Integer sceneNumber;
    private String layerImageUrl;
    private Integer totalLayer;


    public SceneViewDto(FreeWorkScene scene) {
        this.id = scene.getId();
        this.sceneNumber = scene.getSceneNumber();
        this.layerImageUrl = scene.getFirstLayerImgFileUrl();
        this.totalLayer = scene.getTotalLayer();
    }

    public SceneViewDto(EpisodeScene scene) {
        this.id = scene.getId();
        this.sceneNumber = scene.getSceneNumber();
        this.layerImageUrl = scene.getFirstLayerImgFileUrl();
        this.totalLayer = scene.getTotalLayer();
    }


}
