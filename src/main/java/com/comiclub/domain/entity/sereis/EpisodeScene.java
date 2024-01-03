package com.comiclub.domain.entity.sereis;

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
        name = "ep_scene",
        uniqueConstraints={
        @UniqueConstraint(
                name = "episode_id_scene_num_unique",
                columnNames = {"sceneNumber", "episode_id"}
        )
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EpisodeScene extends BaseDateEntity {

    @Id
    @GeneratedValue
    @Column(name = "ep_scene_id")
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id", nullable = false)
    private Episode episode;

    @Column(nullable = false)
    private Long memberId;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "scene", fetch = FetchType.LAZY)
    private List<EpisodeLayer> layers = new ArrayList<>();

    @Range(min = 0, max = 100)
    @Column(nullable = false, columnDefinition = "tinyint")
    private Integer sceneNumber;

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


    public EpisodeScene(Long id) {
        this.id = id;
    }


    private EpisodeScene(Episode episode, Long memberId, Integer sceneNumber, Integer totalLayer, String audioFileUrl, String firstLayerImgFileUrl, Double duration) {
        this.episode = episode;
        this.memberId = memberId;
        this.sceneNumber = sceneNumber;
        this.totalLayer = totalLayer;
        this.audioFileUrl = audioFileUrl;
        this.firstLayerImgFileUrl = firstLayerImgFileUrl;
        this.duration = duration;
    }

    public static EpisodeScene createNewEpisodeScene(Episode episode, Long memberId, Integer sceneNumber, Integer totalLayer, String audioFileUrl, String firstLayerImgFileUrl, Double duration){
        return new EpisodeScene(
                episode,
                memberId,
                sceneNumber,
                totalLayer,
                audioFileUrl,
                firstLayerImgFileUrl,
                duration
        );
    }



    public void changeDuration(Double duration) {
        this.duration = duration;
    }

    public void updateDuration(Map<Long, Double> durationMap) {
        double sceneDuration = 0;

        for (EpisodeLayer layer: layers) {
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

    public void adjustLayerNumbers(Integer layerNumber) {
        for (EpisodeLayer layer: layers) {
            if(layer.getLayerNumber() > layerNumber)
                layer.decreaseLayerNumber();
        }
    }


}
