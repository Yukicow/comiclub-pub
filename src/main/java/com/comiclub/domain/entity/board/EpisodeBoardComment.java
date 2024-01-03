package com.comiclub.domain.entity.board;


import com.comiclub.domain.entity.BaseDateEntity;
import com.comiclub.web.contoller.common.dto.CommentSaveDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@Table(name = "episode_board_comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EpisodeBoardComment extends BaseDateEntity {

    @Id @GeneratedValue
    @Column(name = "epb_comment_id")
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(name = "episode_board_id", nullable = false)
    private Long boardId;

    private Long motherCommentId;

    @Column(nullable = false, length = 15)
    private String writer;

    @Column(nullable = false, length = 1000)
    private String content;

    @ColumnDefault("0")
    @Column(nullable = false, columnDefinition = "MEDIUMINT")
    private Integer totalReply;

    @ColumnDefault("0")
    @Column(nullable = false, columnDefinition = "MEDIUMINT")
    private Integer totalLike;

    @ColumnDefault("0")
    @Column(nullable = false, columnDefinition = "MEDIUMINT")
    private Integer totalUnlike;


    public EpisodeBoardComment(Long id) {
        this.id = id;
    }

    private EpisodeBoardComment(Long memberId, Long boardId, Long motherCommentId, String writer, String content,
                                Integer totalReply, Integer totalLike, Integer totalUnlike) {
        this.memberId = memberId;
        this.boardId = boardId;
        this.motherCommentId = motherCommentId;
        this.writer = writer;
        this.content = content;
        this.totalReply = totalReply;
        this.totalLike = totalLike;
        this.totalUnlike = totalUnlike;
    }

    public static EpisodeBoardComment createNewEpisodeBoardComment(Long memberId, Long episodeBoardId, Long motherCommentId, String writer, String content){
        return new EpisodeBoardComment(
                memberId,
                episodeBoardId,
                motherCommentId,
                writer,
                content,
                0,0,0
        );
    }

    public static EpisodeBoardComment createNewEpisodeBoardComment(CommentSaveDto saveDto){
        return new EpisodeBoardComment(
                saveDto.getMemberId(),
                saveDto.getBoardId(),
                saveDto.getMotherCommentId(),
                saveDto.getWriter(),
                saveDto.getContent(),
                0,0,0
        );
    }



    public void changeContent(String content) {
        this.content = content;
    }
}
