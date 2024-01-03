package com.comiclub.domain.entity.board;


import com.comiclub.domain.entity.BaseDateEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@Table(name = "episode_board")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EpisodeBoard extends BaseDateEntity {

    @Id @GeneratedValue
    @Column(name = "episode_board_id")
    private Long id;

    @Column(nullable = false)
    private Long seriesId;

    @Column(nullable = false)
    private Long episodeId;

    @Column(nullable = false, columnDefinition = "smallint")
    private Integer episodeNumber;

    @Column(nullable = false)
    private Long memberId;

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
    @Column(nullable = false, columnDefinition = "MEDIUMINT")
    private Long totalView;


    public EpisodeBoard(Long id) {
        this.id = id;
    }


    private EpisodeBoard(Long memberId, Long seriesId, Long episodeId, Integer episodeNumber, String title, String writer, String thumbnailUrl, String authorWords,
                        Boolean adultOnly, Boolean pub, Integer totalComment, Integer totalLike, Long totalView) {
        this.memberId = memberId;
        this.seriesId = seriesId;
        this.episodeId = episodeId;
        this.episodeNumber = episodeNumber;
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

    public static EpisodeBoard createNewEpisodeBoard(Long memberId, Long seriesId, Long episodeId, Integer episodeNumber,
                                                     String title, String writer, String thumbnailUrl, String authorWords, Boolean adultOnly, Boolean pub){

        return new EpisodeBoard(
                memberId,
                seriesId,
                episodeId,
                episodeNumber,
                title,
                writer,
                thumbnailUrl,
                authorWords,
                adultOnly,
                pub,
                0, 0, 0L
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
