package com.comiclub.domain.entity.histroy;

import com.comiclub.domain.entity.BaseDateEntity;
import com.comiclub.domain.entity.member.Member;
import com.comiclub.domain.entity.board.EpisodeBoardComment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
        name = "ebc_like_history",
        uniqueConstraints = {
            @UniqueConstraint(
                name = "uk_member_ebc_comment_is_like",
                columnNames = {"member_id", "eb_comment_id", "isLike"}
            )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EBCLikeHistory extends BaseDateEntity {

    @Id @GeneratedValue
    @Column(name = "ebc_like_history_id")
    private Long id;

    @Column(name = "eb_comment_id", nullable = false)
    private Long commentId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Boolean isLike;


    private EBCLikeHistory(Long commentId, Long memberId, Boolean isLike) {
        this.commentId = commentId;
        this.memberId = memberId;
        this.isLike = isLike;
    }

    public static EBCLikeHistory createNewEBCLikeHistory(Long commentId, Long memberId, Boolean isLike) {
        return new EBCLikeHistory(
                commentId,
                memberId,
                isLike
        );
    }
}
