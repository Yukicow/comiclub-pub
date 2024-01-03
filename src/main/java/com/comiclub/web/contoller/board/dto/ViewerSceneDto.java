package com.comiclub.web.contoller.board.dto;

import com.comiclub.domain.entity.freework.FreeWorkScene;
import com.comiclub.domain.entity.sereis.EpisodeScene;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ViewerSceneDto {

    private Integer sceneNumber;
    private Integer totalLayer;
    private String audioFileUrl;
    private List<ViewerLayerDto> layers;

    public ViewerSceneDto(EpisodeScene scene) {
        List<ViewerLayerDto> layers = new ArrayList<>();
        scene.getLayers().forEach(layer -> {
            ViewerLayerDto viewerLayerDto = new ViewerLayerDto(layer);
            layers.add(viewerLayerDto);
        });
        this.sceneNumber = scene.getSceneNumber();
        this.totalLayer = scene.getTotalLayer();
        this.audioFileUrl = scene.getAudioFileUrl();
        this.layers = layers;
    }

    public ViewerSceneDto(FreeWorkScene scene) {
        List<ViewerLayerDto> layers = new ArrayList<>();
        scene.getLayers().forEach(layer -> {
            ViewerLayerDto viewerLayerDto = new ViewerLayerDto(layer);
            layers.add(viewerLayerDto);
        });
        this.sceneNumber = scene.getSceneNumber();
        this.totalLayer = scene.getTotalLayer();
        this.audioFileUrl = scene.getAudioFileUrl();
        this.layers = layers;
    }
}
