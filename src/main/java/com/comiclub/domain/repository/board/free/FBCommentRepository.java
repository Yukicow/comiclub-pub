package com.comiclub.domain.repository.board.free;


import com.comiclub.domain.entity.board.FreeBoardComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FBCommentRepository extends JpaRepository<FreeBoardComment, Long> {

    Optional<FreeBoardComment> findByIdAndBoardId(Long id, Long boardId);

    @Query("SELECT c FROM FreeBoardComment c WHERE c.boardId = :boardId AND c.motherCommentId = :motherCommentId")
    Slice<FreeBoardComment> findSliceOfMoreReplies(@Param("boardId") Long boardId, @Param("motherCommentId") Long motherCommentId, Pageable pageable);

    @Query("SELECT c FROM FreeBoardComment c WHERE c.boardId = :boardId AND c.motherCommentId = null")
    Page<FreeBoardComment> findPageOfMoreComments(@Param("boardId") Long boardId, Pageable pageable);

    @Query(value = "SELECT c.totalReply FROM FreeBoardComment c WHERE c.id = :id")
    Long findTotalReplyById(@Param("id") Long commentId);

    @Modifying
    @Query(value = "UPDATE FreeBoardComment c SET c.totalLike = c.totalLike + 1 WHERE c.id = :id")
    void increaseTotalLike(@Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE FreeBoardComment c SET c.totalLike = c.totalLike - 1 WHERE c.id = :id")
    void decreaseTotalLike(@Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE FreeBoardComment c SET c.totalUnlike = c.totalUnlike + 1 WHERE c.id = :id")
    void increaseTotalUnlike(@Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE FreeBoardComment c SET c.totalUnlike = c.totalUnlike - 1 WHERE c.id = :id")
    void decreaseTotalUnlike(@Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE FreeBoardComment c SET c.totalReply = c.totalReply + 1 WHERE c.id = :id")
    void increaseTotalReply(@Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE FreeBoardComment c SET c.totalReply = c.totalReply - 1 WHERE c.id = :id")
    void decreaseTotalReply(@Param("id") Long id);

}
