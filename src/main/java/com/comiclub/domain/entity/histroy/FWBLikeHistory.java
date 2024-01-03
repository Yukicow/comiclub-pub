package com.comiclub.domain.entity.histroy;

import com.comiclub.domain.entity.BaseDateEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@Table(
        name = "fwb_like_history",
        uniqueConstraints = {
            @UniqueConstraint(
                name = "uk_member_fwb",
                columnNames = {"member_id", "free_work_board_id"}
            )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FWBLikeHistory extends BaseDateEntity {

    @Id @GeneratedValue
    @Column(name = "fwb_like_history_id")
    private Long id;

    @Column(name = "free_work_board_id", nullable = false)
    private Long boardId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;


    private FWBLikeHistory(Long boardId, Long memberId) {
        this.boardId = boardId;
        this.memberId = memberId;
    }

    public static FWBLikeHistory createNewFWBLikeHistory(Long boardId, Long memberId) {
        return new FWBLikeHistory(
                boardId,
                memberId
        );
    }
}
