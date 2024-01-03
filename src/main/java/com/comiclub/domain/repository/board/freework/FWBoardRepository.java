package com.comiclub.domain.repository.board.freework;

import com.comiclub.domain.entity.board.FreeWorkBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

@Repository
public interface FWBoardRepository extends JpaRepository<FreeWorkBoard, Long>, JpaFWBoardRepository {

    boolean existsByFreeWorkId(Long workId);

    @Modifying
    @Query("UPDATE FreeWorkBoard f SET f.totalComment = f.totalComment + 1 WHERE f.id = :boardId")
    void increaseTotalComment(@Param("boardId") Long boardId);

    @Modifying
    @Query("UPDATE FreeWorkBoard f SET f.totalComment = f.totalComment - :amount WHERE f.id = :boardId")
    void decreaseTotalComment(@Param("boardId") Long boardId, @Param("amount") Long amount);

    @Modifying
    @Query("UPDATE FreeWorkBoard f SET f.totalLike = f.totalLike + 1 WHERE f.id = :boardId")
    void increaseTotalLike(@Param("boardId") Long boardId);

    @Modifying
    @Query("UPDATE FreeWorkBoard f SET f.totalLike = f.totalLike - :amount WHERE f.id = :boardId")
    void decreaseTotalLike(@Param("boardId") Long boardId, @Param("amount") int amount);

    @Modifying
    @Query("UPDATE FreeWorkBoard f SET f.totalView = f.totalView + 1 WHERE f.id = :id")
    void increaseTotalView(@Param("id") Long id);
}
