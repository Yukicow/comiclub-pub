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
@Table(name = "series")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Series extends BaseDateEntity {

    @Id
    @GeneratedValue
    @Column(name = "series_id")
    private Long id;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @Column(nullable = false)
    private Long memberId;

    @ColumnDefault("false")
    @Column(nullable = false)
    private Boolean adultOnly;

    @Size(min = 1, max = 40)
    @Column(nullable = false, length = 40)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private String writer;

    private String thumbnailUrl;

    @Column(nullable = false)
    private Boolean pub;

    @Size(max = 110)
    @Column(length = 110)
    private String tag;
    @ColumnDefault("0")
    @Column(nullable = false)
    private Integer totalFollower;
    @ColumnDefault("0")
    @Column(nullable = false, columnDefinition = "MEDIUMINT")
    private Integer totalLike;

    @ColumnDefault("0")
    @Column(nullable = false, columnDefinition = "MEDIUMINT")
    private Long totalView;

    @ColumnDefault("0")
    @Column(nullable = false, columnDefinition = "SMALLINT")
    private Integer totalEp;

    @ColumnDefault("0")
    @Column(nullable = false, columnDefinition = "SMALLINT")
    private Integer totalEpBoard;



    public Series(Long id) {
        this.id = id;
    }

    private Series(Long memberId, Boolean adultOnly, String name, String description, String writer, String thumbnailUrl, boolean pub, String tag,
                  Integer totalFollower, Integer totalLike, Long totalView, Integer totalEp, Integer totalEpBoard) {
        this.memberId = memberId;
        this.adultOnly = adultOnly;
        this.name = name;
        this.description = description;
        this.writer = writer;
        this.thumbnailUrl = thumbnailUrl;
        this.pub = pub;
        this.tag = tag;
        this.totalFollower = totalFollower;
        this.totalLike = totalLike;
        this.totalView = totalView;
        this.totalEp = totalEp;
        this.totalEpBoard = totalEpBoard;
    }

    public static Series createNewSeries(Long memberId, Boolean adultOnly, String name, String description, String writer, String thumbnailUrl, boolean pub, String tag){
        return new Series(
                memberId,
                adultOnly,
                name,
                description,
                writer,
                thumbnailUrl,
                pub,
                tag,
                0,0,0L,0,0
        );
    }

    public void changeThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    public void changePub(boolean pub) {
        this.pub = pub;
    }
    public void changeSeriesName(String name) {
        this.name = name;
    }
    public void changeDescription(String description) {
        this.description = description;
    }
    public void changeAdultOnly(boolean adultOnly) {
        this.adultOnly = adultOnly;
    }
    public void changeTag(String tag) {
        this.tag = tag;
    }

}
