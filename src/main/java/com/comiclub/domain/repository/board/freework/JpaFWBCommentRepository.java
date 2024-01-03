package com.comiclub.domain.repository.board.freework;

import com.comiclub.domain.entity.board.FreeWorkBoardComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaFWBCommentRepository {

    @Query("SELECT c FROM FreeWorkBoardComment c WHERE c.boardId = :boardId AND c.motherCommentId = null")
    Page<FreeWorkBoardComment> findPageOfMoreComments(@Param("boardId") Long boardId, Pageable pageable);

    @Query("SELECT c FROM FreeWorkBoardComment c WHERE c.boardId = :boardId AND c.motherCommentId = :motherCommentId")
    Slice<FreeWorkBoardComment> findSliceOfMoreComments(@Param("boardId") Long boardId, @Param("motherCommentId") Long motherCommentId , Pageable pageable);
}
