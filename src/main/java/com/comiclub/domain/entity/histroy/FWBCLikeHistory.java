package com.comiclub.domain.entity.histroy;

import com.comiclub.domain.entity.BaseDateEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "fwbc_like_history",
        uniqueConstraints = {
            @UniqueConstraint(
                name = "uk_member_fwbc_comment_is_like",
                columnNames = {"member_id", "fwb_comment_id", "isLike"}
            )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FWBCLikeHistory extends BaseDateEntity {

    @Id @GeneratedValue
    @Column(name = "fwbc_like_history_id")
    private Long id;

    @Column(name = "fwb_comment_id", nullable = false)
    private Long commentId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Boolean isLike;


    public FWBCLikeHistory(Long commentId, Long memberId, Boolean isLike) {
        this.commentId = commentId;
        this.memberId = memberId;
        this.isLike = isLike;
    }

    public static FWBCLikeHistory createNewFWBCLikeHistory(Long commentId, Long memberId, Boolean isLike) {
        return new FWBCLikeHistory(
                commentId,
                memberId,
                isLike
        );
    }
}
