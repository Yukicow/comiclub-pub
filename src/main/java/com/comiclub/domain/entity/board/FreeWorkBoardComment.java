package com.comiclub.domain.entity.board;


import com.comiclub.domain.entity.BaseDateEntity;
import com.comiclub.domain.entity.member.Member;
import com.comiclub.web.contoller.common.dto.CommentSaveDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "free_work_board_comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FreeWorkBoardComment extends BaseDateEntity {

    @Id @GeneratedValue
    @Column(name = "fwb_comment_id")
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(name = "free_work_board_id", nullable = false)
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

    public FreeWorkBoardComment(Long id) {
        this.id = id;
    }

    private FreeWorkBoardComment(Long memberId, Long boardId, Long motherCommentId, String writer, String content,
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

    public static FreeWorkBoardComment createNewFreeWorkBoardComment(CommentSaveDto saveDto){
        return new FreeWorkBoardComment(
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
