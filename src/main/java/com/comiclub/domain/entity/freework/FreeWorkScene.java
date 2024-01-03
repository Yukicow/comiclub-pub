package com.comiclub.domain.entity.freework;

import com.comiclub.domain.entity.BaseDateEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.constraints.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Table(
        name = "fw_scene",
        uniqueConstraints={
        @UniqueConstraint(
                name = "free_work_id_scene_num_unique",
                columnNames = {"sceneNumber", "free_work_id"}
        )
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FreeWorkScene extends BaseDateEntity {

    @Id
    @GeneratedValue
    @Column(name = "fw_scene_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "free_work_id", nullable = false)
    private FreeWork freeWork;

    @Column(nullable = false)
    private Long memberId;

    @Range(min = 0, max = 100)
    @Column(nullable = false, columnDefinition = "tinyint")
    private Integer sceneNumber;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "scene", fetch = FetchType.LAZY)
    private List<FreeWorkLayer> layers = new ArrayList<>();

    @ColumnDefault("0")
    @Range(min = 1, max = 4)
    @Column(nullable = false, columnDefinition = "tinyint")
    private Integer totalLayer;

    private String audioFileUrl;

    private String firstLayerImgFileUrl;

    @ColumnDefault("0")
    @Range(min = 0, max = 40)
    @Column(nullable = false)
    private Double duration;


    @Version
    private long version;


    public FreeWorkScene(Long id) {
        this.id = id;
    }

    private FreeWorkScene(FreeWork freeWork, Long memberId, Integer sceneNumber, Integer totalLayer, String audioFileUrl, String firstLayerImgFileUrl, Double duration) {
        this.freeWork = freeWork;
        this.memberId = memberId;
        this.sceneNumber = sceneNumber;
        this.totalLayer = totalLayer;
        this.audioFileUrl = audioFileUrl;
        this.firstLayerImgFileUrl = firstLayerImgFileUrl;
        this.duration = duration;
    }

    public static FreeWorkScene createNewFreeWorkScene(FreeWork freeWork, Long memberId, Integer sceneNumber, Integer totalLayer, String audioFileUrl, String firstLayerImgFileUrl, Double duration){
        return new FreeWorkScene(
                freeWork,
                memberId,
                sceneNumber,
                totalLayer,
                audioFileUrl,
                firstLayerImgFileUrl,
                duration
        );
    }


    public void changeDuration(double duration){
        this.duration = duration;
    }

    public void updateDuration(Map<Long, Double> durationMap) {
        double sceneDuration = 0;

        for (FreeWorkLayer layer: layers) {
            double layerDuration = durationMap.get(layer.getId());
            layer.changeDuration(layerDuration);
            sceneDuration += layerDuration;
        }

        this.duration = sceneDuration;
    }

    public void changeAudioFileUrl(String audioFileUrl) {
        this.audioFileUrl = audioFileUrl;
    }

    public void changeFirstLayerImgFileUrl(String firstLayerImgFileUrl) {
        this.firstLayerImgFileUrl = firstLayerImgFileUrl;
    }

    public void increaseTotalLayer() {
        this.totalLayer += 1;
    }

    public void decreaseTotalLayer() {
        this.totalLayer -= 1;
    }

    public void adjustLayerNumbers(int layerNumber) {
        for (FreeWorkLayer layer: layers) {
            if(layer.getLayerNumber() > layerNumber)
                layer.changeLayerNumber(layer.getLayerNumber() - 1);
        }
    }


}
