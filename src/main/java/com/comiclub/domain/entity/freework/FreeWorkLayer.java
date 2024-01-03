package com.comiclub.domain.entity.freework;

import com.comiclub.domain.entity.BaseDateEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.validator.constraints.Range;

@Entity
@Getter
@Table(
        name = "fw_layer",
        uniqueConstraints={
                @UniqueConstraint(
                        name = "fw_scene_id_layer_num_unique",
                        columnNames = {"layerNumber", "scene_id"}
                )})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FreeWorkLayer extends BaseDateEntity {

    @Id
    @GeneratedValue
    @Column(name = "fw_layer_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_id", nullable = false)
    private FreeWorkScene scene;

    @Column(nullable = false)
    private Long memberId;

    @ColumnDefault("0")
    @Range(min = 0, max = 10)
    @Column(nullable = false)
    private Double duration;

    private String imgFileUrl;

    @Range(min = 1, max = 4)
    @Column(nullable = false)
    private Integer layerNumber;


    @Version
    private long version;


    public FreeWorkLayer(Long id) {
        this.id = id;
    }

    private FreeWorkLayer(FreeWorkScene scene, Long memberId, Integer layerNumber, Double duration, String imgFileUrl) {
        this.scene = scene;
        this.memberId = memberId;
        this.layerNumber = layerNumber;
        this.duration = duration;
        this.imgFileUrl = imgFileUrl;
    }

    public static FreeWorkLayer createNewFreeWorkLayer(FreeWorkScene scene, Long memberId, Integer layerNumber, Double duration, String imgFileUrl){
        return new FreeWorkLayer(
                scene,
                memberId,
                layerNumber,
                duration,
                imgFileUrl
        );
    }

    public void changeImageFileUrl(String imgFileUrl){
        this.imgFileUrl = imgFileUrl;
    }

    public void changeDuration(Double duration){
        this.duration = duration;
    }
    public void changeLayerNumber(int layerNumber) {
        this.layerNumber = layerNumber;
    }

    public void save(){
        this.scene.increaseTotalLayer();
    }
    public void delete() {
        scene.decreaseTotalLayer();
        scene.changeDuration(scene.getDuration() - this.getDuration());
        scene.adjustLayerNumbers(layerNumber);
    }


}
