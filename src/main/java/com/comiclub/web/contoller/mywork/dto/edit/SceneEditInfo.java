package com.comiclub.web.contoller.mywork.dto.edit;


import com.comiclub.web.contoller.mywork.dto.SceneDto;
import lombok.Data;

@Data
public class SceneEditInfo {

    private SceneDto scene;
    private boolean hasNextScene;
    private boolean hasPrevScene;

    public SceneEditInfo(SceneDto scene, boolean hasNextScene, boolean hasPrevScene) {
        this.scene = scene;
        this.hasNextScene = hasNextScene;
        this.hasPrevScene = hasPrevScene;
    }
}
