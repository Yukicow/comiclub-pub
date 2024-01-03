package com.comiclub.domain.entity.histroy;


import com.comiclub.domain.entity.member.Member;
import com.comiclub.domain.entity.sereis.Series;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Table(
        name = "following_history",
        uniqueConstraints = {
            @UniqueConstraint(
                name = "uk_member_series",
                columnNames = {"member_id", "series_id"}
            )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowHistory {

    @Id
    @GeneratedValue
    @Column(name = "follow_history_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "series_id", nullable = false)
    private Long seriesId;

    @Column(nullable = false)
    private boolean adultOnly;

    public FollowHistory(Long memberId, Long seriesId, boolean adultOnly) {
        this.memberId = memberId;
        this.seriesId = seriesId;
        this.adultOnly = adultOnly;
    }

    public static FollowHistory createNewFollowHistory(Long memberId, Long seriesId, boolean adultOnly){
        return new FollowHistory(
                memberId,
                seriesId,
                adultOnly
        );
    }
}
