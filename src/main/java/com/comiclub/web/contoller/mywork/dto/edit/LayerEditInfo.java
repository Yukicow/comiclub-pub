package com.comiclub.web.contoller.mywork.dto.edit;

import com.comiclub.web.contoller.mywork.dto.LayerDto;
import lombok.Data;

@Data
public class LayerEditInfo {

    private LayerDto layer;
    private Boolean hasNextLayer;
    private Boolean hasPrevLayer;

    public LayerEditInfo(LayerDto layer, Boolean hasNextLayer, Boolean hasPrevLayer) {
        this.layer = layer;
        this.hasNextLayer = hasNextLayer;
        this.hasPrevLayer = hasPrevLayer;
    }
}
