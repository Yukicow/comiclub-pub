package com.comiclub.domain.repository.series;

import com.comiclub.domain.entity.histroy.FollowHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface JpaFollowHistoryRepository {


    public Page<FollowHistory> findFollowList(Long memberId, boolean viewAdult, Pageable pageable);

}
