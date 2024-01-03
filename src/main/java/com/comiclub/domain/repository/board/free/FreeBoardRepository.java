package com.comiclub.domain.repository.board.free;

import com.comiclub.domain.entity.board.FreeBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FreeBoardRepository extends JpaRepository<FreeBoard, Long>, JpaFreeBoardRepository{

    Optional<FreeBoard> findByIdAndClubId(Long boardId, Long clubId);

    @Modifying
    @Query(value = "UPDATE FreeBoard b SET b.totalView = totalView + 1 WHERE b.id = :id")
    void increaseTotalView(@Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE FreeBoard b SET b.totalComment = b.totalComment + 1 WHERE b.id = :id")
    void increaseTotalComment(@Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE FreeBoard b SET totalComment = totalComment - :amount WHERE b.id = :id")
    void decreaseTotalComment(@Param("id") Long id, @Param("amount") Long amount);
    @Modifying
    @Query(value = "UPDATE FreeBoard b SET totalLike = totalLike + 1 WHERE b.id = :id")
    void increaseTotalLike(@Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE FreeBoard b SET totalLike = totalLike - 1 WHERE b.id = :id")
    void decreaseTotalLike(@Param("id") Long id);

}
