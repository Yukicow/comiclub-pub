package com.comiclub.domain.repository.board.episode;

import com.comiclub.domain.entity.histroy.EBLikeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EBLikeHistoryRepository extends JpaRepository<EBLikeHistory, Long> {
    boolean existsByBoardIdAndMemberId(Long boardId, Long memberId);

    @Query("SELECT h FROM EBLikeHistory h WHERE h.boardId = :boardId AND h.memberId = :memberId")
    Optional<EBLikeHistory> findHistory(@Param("boardId") Long boardId, @Param("memberId") Long memberId);
}
