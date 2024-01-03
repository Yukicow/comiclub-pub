package com.comiclub.domain.repository.board.free;

import com.comiclub.domain.entity.histroy.FBCLikeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FBCLikeHistoryRepository extends JpaRepository<FBCLikeHistory, Long> {

    @Query("SELECT h FROM FBCLikeHistory h WHERE h.commentId = :commentId AND h.memberId = :memberId AND h.isLike = :isLike")
    Optional<FBCLikeHistory> findHistory(@Param("memberId") Long memberId, @Param("commentId") Long commentId, @Param("isLike") Boolean isLike);
}
