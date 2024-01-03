package com.comiclub.domain.entity.sereis;


import com.comiclub.domain.entity.BaseDateEntity;
import com.comiclub.domain.entity.member.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "episode",
    uniqueConstraints = {
        @UniqueConstraint(
                name = "series_id_episode_num_unique",
                columnNames = {"series_id", "episodeNumber"}
        )
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Episode extends BaseDateEntity {

    @Id
    @GeneratedValue
    @Column(name = "episode_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "series_id", nullable = false)
    private Long seriesId;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "episode")
    private List<EpisodeScene> scenes = new ArrayList<>();

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "episode")
    private List<EpBackgroundSound> bgSounds = new ArrayList<>();

    @Size(min = 1, max = 50)
    @Column(nullable = false, length = 50)
    private String title;


    @Column(nullable = false, columnDefinition = "smallint")
    private Integer episodeNumber;

    private String thumbnailUrl;

    @ColumnDefault("false")
    @Column(nullable = false)
    private Boolean autoMode;

    @ColumnDefault("false")
    @Column(nullable = false)
    private Boolean freeUse;

    public Episode(Long id) {
        this.id = id;
    }

    private Episode(Long memberId, Long seriesId, String title, Integer episodeNumber,
                   String thumbnailUrl, Boolean autoMode, Boolean freeUse) {
        this.memberId = memberId;
        this.seriesId = seriesId;
        this.title = title;
        this.episodeNumber = episodeNumber;
        this.thumbnailUrl = thumbnailUrl;
        this.autoMode = autoMode;
        this.freeUse = freeUse;
    }

    public static Episode createNewEpisode(Long memberId, Long seriesId, String title, Integer episodeNumber,
                                           String thumbnailUrl, Boolean autoMode, Boolean freeUse){
        return new Episode(
                memberId,
                seriesId,
                title,
                episodeNumber,
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
