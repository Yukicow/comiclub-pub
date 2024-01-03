package com.comiclub.domain.repository.board.episode;

import com.comiclub.domain.entity.board.EpisodeBoardComment;
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
public interface EBCommentRepository extends JpaRepository<EpisodeBoardComment, Long> {

    @Query("SELECT c FROM EpisodeBoardComment c WHERE c.boardId = :boardId AND c.motherCommentId = :motherCommentId")
    Slice<EpisodeBoardComment> findSliceOfMoreComments(@Param("boardId") Long boardId, @Param("motherCommentId") Long motherCommentId, Pageable pageable);

    @Query("SELECT c FROM EpisodeBoardComment c WHERE c.boardId = :boardId AND c.motherCommentId = null")
    Page<EpisodeBoardComment> findPageOfMoreComments(@Param("boardId") Long boardId, Pageable pageable);

    @Modifying
    @Query("UPDATE EpisodeBoardComment c SET c.totalReply = c.totalReply + 1 WHERE c.id = :commentId")
    void increaseTotalReply(@Param("commentId") Long commentId);

    @Modifying
    @Query("UPDATE EpisodeBoardComment c SET c.totalReply = c.totalReply - 1 WHERE c.id = :commentId")
    void decreaseTotalReply(@Param("commentId") Long commentId);

    @Modifying
    @Query("UPDATE EpisodeBoardComment c SET c.totalLike = c.totalLike + 1 WHERE c.id = :commentId")
    void increaseTotalLike(@Param("commentId") Long commentId);

    @Modifying
    @Query("UPDATE EpisodeBoardComment c SET c.totalLike = c.totalLike - 1 WHERE c.id = :commentId")
    void decreaseTotalLike(@Param("commentId") Long commentId);

    @Modifying
    @Query("UPDATE EpisodeBoardComment c SET c.totalUnlike = c.totalUnlike + 1 WHERE c.id = :commentId")
    void increaseTotalUnlike(@Param("commentId") Long commentId);

    @Modifying
    @Query("UPDATE EpisodeBoardComment c SET c.totalUnlike = c.totalUnlike - 1 WHERE c.id = :commentId")
    void decreaseTotalUnlike(@Param("commentId") Long commentId);

    Optional<EpisodeBoardComment> findByIdAndBoardId(Long id, Long boardId);
}
