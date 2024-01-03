package com.comiclub.domain.entity.histroy;

import com.comiclub.domain.entity.BaseDateEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@Table(
        name = "fb_like_history",
        uniqueConstraints = {
            @UniqueConstraint(
                name = "uk_member_fb",
                columnNames = {"member_id", "free_board_id"}
            )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FBLikeHistory extends BaseDateEntity {

    @Id @GeneratedValue
    @Column(name = "fb_like_history_id")
    private Long id;

    @Column(name = "free_board_id", nullable = false)
    private Long boardId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;


    public FBLikeHistory(Long boardId, Long memberId) {
        this.boardId = boardId;
        this.memberId = memberId;
    }

    public static FBLikeHistory createNewFBLikeHistory(Long boardId, Long memberId) {
        return new FBLikeHistory(
                boardId,
                memberId
        );
    }
}
