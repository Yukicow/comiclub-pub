package com.comiclub.domain.entity.histroy;

import com.comiclub.domain.entity.BaseDateEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@Table(
        name = "eb_like_history",
        uniqueConstraints = {
            @UniqueConstraint(
                name = "uk_member_eb",
                columnNames = {"member_id", "episode_board_id"}
            )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EBLikeHistory extends BaseDateEntity {

    @Id @GeneratedValue
    @Column(name = "eb_like_history_id")
    private Long id;

    @Column(name = "episode_board_id", nullable = false)
    private Long boardId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;


    private EBLikeHistory(Long boardId, Long memberId) {
        this.boardId = boardId;
        this.memberId = memberId;
    }

    public static EBLikeHistory createNewEBLikeHistory(Long boardId, Long memberId) {
        return new EBLikeHistory(
                boardId,
                memberId
        );
    }
}
