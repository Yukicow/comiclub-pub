package com.comiclub.domain.entity.freework;


import com.comiclub.domain.entity.BaseDateEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "free_work")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FreeWork extends BaseDateEntity {

    @Id
    @GeneratedValue
    @Column(name = "free_work_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Size(min = 1, max = 40)
    @Column(nullable = false, length = 50)
    private String title;

    private String thumbnailUrl;

    @ColumnDefault("false")
    @Column(nullable = false)
    private Boolean autoMode;

    @ColumnDefault("false")
    @Column(nullable = false)
    private Boolean freeUse;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "freeWork")
    private List<FreeWorkScene> scenes = new ArrayList<>();


    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "freeWork")
    private List<FWBackgroundSound> bgSounds = new ArrayList<>();


    public FreeWork(Long id) {
        this.id = id;
    }

    private FreeWork(Long memberId, String title, String thumbnailUrl, Boolean autoMode, Boolean freeUse) {
        this.memberId = memberId;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.autoMode = autoMode;
        this.freeUse = freeUse;
    }

    public static FreeWork createNewFreeWork(Long memberId, String title, String thumbnailUrl, Boolean autoMode, Boolean freeUse){
        return new FreeWork(
                memberId,
                title,
                thumbnailUrl,
                autoMode,
                freeUse
        );
    }


    public void changeThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void changeTitle(String title) {
        this.title = title;
    }
    public void changeAutoMode(Boolean autoMode) {
        this.autoMode = autoMode;
    }
    public void changeFreeUse(Boolean freeUse) {
        this.freeUse = freeUse;
    }
}
