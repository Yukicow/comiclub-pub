package com.comiclub.domain.entity.sereis;

import com.comiclub.domain.entity.BaseDateEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;


@Entity
@Getter
@Table(name = "ep_bg_sound")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EpBackgroundSound extends BaseDateEntity {

    @Id
    @GeneratedValue
    @Column(name = "ep_bg_sound_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id", nullable = false)
    private Episode episode;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private String fileName;

    private String fileUrl;

    @Range(min = 1, max = 99)
    @Column(nullable = false)
    private Integer startSceneNumber;

    @Range(min = 2, max = 100)
    @Column(nullable = false)
    private Integer endSceneNumber;

    @Version
    private long version;




    private EpBackgroundSound(Episode episode, Long memberId, String fileName, String fileUrl, Integer startSceneNumber, Integer endSceneNumber) {
        this.episode = episode;
        this.memberId = memberId;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.startSceneNumber = startSceneNumber;
        this.endSceneNumber = endSceneNumber;
    }

    public static EpBackgroundSound createNewEpBackgroundSound(Episode episode, Long memberId, String fileName, String fileUrl, Integer startSceneNumber, Integer endSceneNumber){
        return new EpBackgroundSound(
                episode,
                memberId,
                fileName,
                fileUrl,
                startSceneNumber,
                endSceneNumber
        );
    }


    public void changeFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public void changeStartSceneNumber(int startSceneNumber) {
        this.startSceneNumber = startSceneNumber;
    }

    public void changeEndSceneNumber(int endSceneNumber) {
        this.endSceneNumber = endSceneNumber;
    }
}
