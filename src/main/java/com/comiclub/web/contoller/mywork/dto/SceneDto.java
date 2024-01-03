package com.comiclub.web.contoller.mywork.dto;

import com.comiclub.domain.entity.freework.FreeWorkScene;
import com.comiclub.domain.entity.sereis.EpisodeScene;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SceneDto {

    private Long id;

    private Integer sceneNumber;

    private List<LayerDto> layers;

    private Integer totalLayer;

    private String audioFileUrl;

    private Double duration;

    public SceneDto(FreeWorkScene scene) {
        ArrayList<LayerDto> layerDtos = new ArrayList<>();
        scene.getLayers().forEach(layer -> layerDtos.add(new LayerDto(layer)));

        this.id = scene.getId();
        this.sceneNumber = scene.getSceneNumber();
        this.layers = layerDtos;
        this.totalLayer = scene.getTotalLayer();
        this.audioFileUrl = scene.getAudioFileUrl();
        this.duration = scene.getDuration();
    }

    public SceneDto(EpisodeScene scene) {
        ArrayList<LayerDto> layerDtos = new ArrayList<>();
        scene.getLayers().forEach(layer -> layerDtos.add(new LayerDto(layer)));

        this.id = scene.getId();
        this.sceneNumber = scene.getSceneNumber();
        this.layers = layerDtos;
        this.totalLayer = scene.getTotalLayer();
        this.audioFileUrl = scene.getAudioFileUrl();
        this.duration = scene.getDuration();
    }


}
