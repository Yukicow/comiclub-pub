package com.comiclub.domain.repository.series;

import com.comiclub.domain.entity.histroy.FollowHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowHistoryRepository extends JpaRepository<FollowHistory, Long>, JpaFollowHistoryRepository {

    FollowHistory findByMemberIdAndSeriesId(Long memberId, Long seriesId);

    boolean existsByMemberIdAndSeriesId(Long memberId, Long seriesId);
}
