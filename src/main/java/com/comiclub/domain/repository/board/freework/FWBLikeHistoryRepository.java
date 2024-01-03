package com.comiclub.domain.repository.board.freework;

import com.comiclub.domain.entity.histroy.FWBLikeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FWBLikeHistoryRepository extends JpaRepository<FWBLikeHistory, Long> {
    boolean existsByBoardIdAndMemberId(Long boardId, Long memberId);

    @Query("SELECT h FROM FWBLikeHistory h WHERE h.boardId = :boardId AND h.memberId = :memberId")
    Optional<FWBLikeHistory> findHistory(@Param("boardId") Long boardId, @Param("memberId") Long memberId);
}
