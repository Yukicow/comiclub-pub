package com.comiclub.domain.repository.board.episode;

import com.comiclub.domain.entity.histroy.EBCLikeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EBCLikeHistoryRepository extends JpaRepository<EBCLikeHistory, Long> {

    @Query("SELECT h FROM EBCLikeHistory h WHERE h.commentId = :commentId AND h.memberId = :memberId AND h.isLike = :isLike")
    Optional<EBCLikeHistory> findHistory(@Param("commentId") Long commentId, @Param("memberId") Long memberId, @Param("isLike") boolean isLike);

}
