package com.comiclub.domain.repository.board.free;

import com.comiclub.domain.entity.histroy.FBLikeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FBLikeHistoryRepository extends JpaRepository<FBLikeHistory, Long> {

    @Query("SELECT h FROM FBLikeHistory h WHERE h.boardId = :boardId AND h.memberId = :memberId")
    Optional<FBLikeHistory> findHistory(@Param("memberId") Long memberId, @Param("boardId") Long boardId);
}
