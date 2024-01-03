package com.comiclub.domain.repository.board.freework;

import com.comiclub.domain.entity.board.FreeWorkBoardComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FWBCommentRepository extends JpaRepository<FreeWorkBoardComment, Long>, JpaFWBCommentRepository {


    @Modifying
    @Query("UPDATE FreeWorkBoardComment c SET c.totalLike = c.totalLike + 1 WHERE c.id = :commentId")
    void increaseTotalLike(@Param("commentId") Long commentId);

    @Modifying
    @Query("UPDATE FreeWorkBoardComment c SET c.totalLike = c.totalLike - 1 WHERE c.id = :commentId")
    void decreaseTotalLike(@Param("commentId") Long commentId);

    @Modifying
    @Query("UPDATE FreeWorkBoardComment c SET c.totalUnlike = c.totalUnlike + 1 WHERE c.id = :commentId")
    void increaseTotalUnlike(@Param("commentId") Long commentId);

    @Modifying
    @Query("UPDATE FreeWorkBoardComment c SET c.totalUnlike = c.totalUnlike - 1 WHERE c.id = :commentId")
    void decreaseTotalUnlike(@Param("commentId") Long commentId);


    @Modifying
    @Query("UPDATE FreeWorkBoardComment c SET c.totalReply = c.totalReply + 1 WHERE c.id = :commentId")
    void increaseTotalReply(@Param("commentId") Long commentId);

    @Modifying
    @Query("UPDATE FreeWorkBoardComment c SET c.totalReply = c.totalReply - 1 WHERE c.id = :commentId")
    void decreaseTotalReply(@Param("commentId") Long commentId);
}
