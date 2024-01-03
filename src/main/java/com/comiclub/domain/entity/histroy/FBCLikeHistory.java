package com.comiclub.domain.entity.histroy;

import com.comiclub.domain.entity.BaseDateEntity;
import com.comiclub.domain.entity.member.Member;
import com.comiclub.domain.entity.board.FreeBoardComment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
        name = "fbc_like_history",
        uniqueConstraints = {
            @UniqueConstraint(
                name = "uk_member_fbc_comment_is_like",
                columnNames = {"member_id", "fbc_comment_id", "isLike"}
            )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FBCLikeHistory extends BaseDateEntity {

    @Id @GeneratedValue
    @Column(name = "fbc_like_history_id")
    private Long id;

    @Column(name = "fbc_comment_id", nullable = false)
    private Long commentId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Boolean isLike;


    private FBCLikeHistory(Long commentId, Long memberId, Boolean isLike) {
        this.commentId = commentId;
        this.memberId = memberId;
        this.isLike = isLike;
    }

    public static FBCLikeHistory createNewFBCLikeHistory(Long commentId, Long memberId, Boolean isLike) {
        return new FBCLikeHistory(
                commentId,
                memberId,
                isLike
        );
    }
}
