package com.comiclub.domain.entity.freework;

import com.comiclub.domain.entity.BaseDateEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;


@Entity
@Getter
@Table(name = "fw_bg_sound")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FWBackgroundSound extends BaseDateEntity {

    @Id
    @GeneratedValue
    @Column(name = "fw_bg_sound_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "free_work_id", nullable = false)
    private FreeWork freeWork;

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


    private FWBackgroundSound(FreeWork freeWork, Long memberId, String fileName, String fileUrl, Integer startSceneNumber, Integer endSceneNumber) {
        this.freeWork = freeWork;
        this.memberId = memberId;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.startSceneNumber = startSceneNumber;
        this.endSceneNumber = endSceneNumber;
    }

    public static FWBackgroundSound createNewFWBackgroundSound(FreeWork freeWork, Long memberId, String fileName, String fileUrl, Integer startSceneNumber, Integer endSceneNumber){
        return new FWBackgroundSound(
                freeWork,
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
