package com.comiclub.domain.repository.board.freework;

import com.comiclub.domain.entity.histroy.FWBCLikeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FWBCLikeHistoryRepository extends JpaRepository<FWBCLikeHistory, Long> {

    @Query("SELECT h FROM FWBCLikeHistory h WHERE h.commentId = :commentId AND h.memberId = :memberId AND h.isLike = :isLike")
    Optional<FWBCLikeHistory> findHistory(@Param("commentId") Long commentId, @Param("memberId") Long memberId, @Param("isLike") boolean isLike);

}
