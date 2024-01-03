package com.comiclub.domain.entity.board;


import com.comiclub.domain.entity.BaseDateEntity;
import com.comiclub.domain.entity.member.Member;
import com.comiclub.domain.entity.freework.FreeWork;
import jakarta.persistence.*;
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
@Table(name = "free_work_board")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FreeWorkBoard extends BaseDateEntity {

    @Id @GeneratedValue
    @Column(name = "free_work_board_id")
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long freeWorkId;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, length = 15)
    private String writer;

    private String thumbnailUrl;

    private String authorWords;

    @Column(nullable = false)
    private Boolean adultOnly;

    @Column(nullable = false)
    private Boolean pub;

    @ColumnDefault("0")
    @Column(nullable = false, columnDefinition = "MEDIUMINT")
    private Integer totalComment;

    @ColumnDefault("0")
    @Column(nullable = false, columnDefinition = "MEDIUMINT")
    private Integer totalLike;

    @ColumnDefault("0")
    @Column(nullable = false)
    private Long totalView;


    public FreeWorkBoard(Long id) {
        this.id = id;
    }

    private FreeWorkBoard(Long memberId, Long freeWorkId, String title, String writer, String thumbnailUrl, String authorWords,
                         Boolean adultOnly, Boolean pub, Integer totalComment, Integer totalLike, Long totalView) {
        this.memberId = memberId;
        this.freeWorkId = freeWorkId;
        this.title = title;
        this.writer = writer;
        this.thumbnailUrl = thumbnailUrl;
        this.authorWords = authorWords;
        this.adultOnly = adultOnly;
        this.pub = pub;
        this.totalComment = totalComment;
        this.totalLike = totalLike;
        this.totalView = totalView;
    }

    public static FreeWorkBoard createNewFreeWorkBoard(Long memberId, Long freeWorkId, String title, String writer, String thumbnailUrl, String authorWords,
                                                       Boolean adultOnly, Boolean pub){
        return new FreeWorkBoard(
                memberId,
                freeWorkId,
                title,
                writer,
                thumbnailUrl,
                authorWords,
                adultOnly,
                pub,
                0,0,0L
        );
    }


    public void changeTitle(String title) {
        this.title = title;
    }
    public void changeAuthorWords(String authorWords) {
        this.authorWords = authorWords;
    }
    public void changeAdultOnly(boolean adultOnly) {
        this.adultOnly = adultOnly;
    }
    public void changePub(boolean pub) {
        this.pub = pub;
    }
    public void changeThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

}
