package com.comiclub.domain.service.board;


import com.comiclub.domain.entity.board.FreeBoard;
import com.comiclub.domain.entity.board.FreeBoardComment;
import com.comiclub.domain.entity.histroy.FBCLikeHistory;
import com.comiclub.domain.entity.histroy.FBLikeHistory;
import com.comiclub.domain.repository.board.free.FBLikeHistoryRepository;
import com.comiclub.domain.repository.board.free.FBCLikeHistoryRepository;
import com.comiclub.web.contoller.club.form.FreeBoardForm;
import com.comiclub.web.contoller.common.dto.CommentDto;
import com.comiclub.web.contoller.common.dto.CommentSaveDto;
import com.comiclub.web.contoller.common.dto.DecreaseInfo;
import com.comiclub.web.exception.NoAuthorizationException;
import com.comiclub.web.exception.NotFoundException;
import com.comiclub.domain.repository.board.free.FreeBoardRepository;
import com.comiclub.domain.repository.board.free.FBCommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FreeBoardService {

    private final FreeBoardRepository boardRepository;
    private final FBLikeHistoryRepository boardLikeHistoryRepository;
    private final FBCommentRepository commentRepository;
    private final FBCLikeHistoryRepository commentHistoryRepository;





    /**
     * Board Option
     * */



    public void saveBoard(FreeBoard freeBoard) {
        boardRepository.save(freeBoard);
    }
    public void deleteBoard(FreeBoard freeBoard) {
        boardRepository.delete(freeBoard);
    }

    public void increaseTotalView(Long boardId) {
        boardRepository.increaseTotalView(boardId);
    }

    public String likeBoard(Long boardId, Long memberId) {
        // 게시물이 있는 지 없는 지 확인 해야 함 ( 이 과정을 생략하면 History를 검색할 떄 이력이 없는 것으로 동작해 무조건 Decrease를 호출하게 되기 때문 )
        boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("There is no board id '" + boardId + "'"));

        FBLikeHistory history = boardLikeHistoryRepository.findHistory(memberId, boardId)
                .orElse(null);

        if (history == null) {
            FBLikeHistory newHistory = FBLikeHistory.createNewFBLikeHistory(boardId, memberId);
            boardLikeHistoryRepository.save(newHistory);
            boardRepository.increaseTotalLike(boardId);
            return "INCREASE";
        }else{
            boardLikeHistoryRepository.delete(history);
            boardRepository.decreaseTotalLike(boardId);
            return "DECREASE";
        }
    }


    public void updateFreeBoard(Long boardId, FreeBoardForm form) {
        FreeBoard freeBoard = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("There is no comments with page '" + boardId + "'"));

        freeBoard.changeTopic(form.getTopic());
        freeBoard.changeTitle(form.getTitle());
        freeBoard.changeWriter(form.getWriter());
        freeBoard.changePassword(form.getPassword());
        freeBoard.changeAdultOnly(form.getAdultOnly());
        freeBoard.changeContent(form.getContent());
    }



    /**
     * Comment Option
     * */

    @Transactional(readOnly = true)
    public Slice<CommentDto> getSliceOfMoreComments(Long boardId, Long motherCommentId, Pageable pageable) {

        Slice<FreeBoardComment> slice = commentRepository.findSliceOfMoreReplies(boardId, motherCommentId, pageable);

        List<CommentDto> content = slice.stream()
                .map(comment -> new CommentDto(comment))
                .collect(Collectors.toList());

        return new SliceImpl<>(content, pageable, slice.hasNext());
    }

    @Transactional(readOnly = true)
    public Page<CommentDto> getPageOfMoreComments(Long boardId, Long motherCommentId, Pageable pageable) {

        Page<FreeBoardComment> page = commentRepository.findPageOfMoreComments(boardId, pageable);

        List<CommentDto> content = page.stream()
                .map(comment -> new CommentDto(comment))
                .collect(Collectors.toList());

        return PageableExecutionUtils.getPage(content, pageable, () -> page.getTotalPages());
    }


    public CommentDto writeComment(CommentSaveDto dto) {
        FreeBoardComment comment = FreeBoardComment.createNewFreeBoardComment(dto);

        commentRepository.save(comment);

        boardRepository.increaseTotalComment(comment.getBoardId());

        return new CommentDto(comment);
    }

    public CommentDto writeReply(CommentSaveDto dto) {
        commentRepository.findByIdAndBoardId(dto.getMotherCommentId(), dto.getBoardId())
                .orElseThrow(() -> new NotFoundException("Bad Request"));

        FreeBoardComment reply = FreeBoardComment.createNewFreeBoardComment(dto);

        commentRepository.save(reply);

        boardRepository.increaseTotalComment(reply.getBoardId());
        commentRepository.increaseTotalReply(reply.getMotherCommentId());

        return new CommentDto(reply);
    }

    public void updateFreeBoardComment(Long commentId, Long memberId, String content) {
        FreeBoardComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("There is no comment id '" + commentId + "'"));

        if (!comment.getMemberId().equals(memberId)) {
            throw new NoAuthorizationException("No authorization for modifying comment");
        }

        comment.changeContent(content);
    }


    public DecreaseInfo deleteComment(Long commentId, Long memberId) {

        FreeBoardComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("There is no comment id '" + commentId + "'"));

        if (!comment.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for deleting comment");

        if (comment.getMotherCommentId() == null) {
            commentRepository.delete(comment);
            boardRepository.decreaseTotalComment(comment.getBoardId(), comment.getTotalReply() + 1L);
            return new DecreaseInfo(null, comment.getTotalReply() + 1L);
        }else{
            commentRepository.delete(comment);
            commentRepository.decreaseTotalReply(comment.getMotherCommentId());
            boardRepository.decreaseTotalComment(comment.getBoardId(), 1L);
            return new DecreaseInfo(comment.getMotherCommentId(), 1L);
        }
    }


    public String likeComment(Long commentId, Long memberId) {
        // 댓글이 있는 지 없는 지 확인 해야 함 ( 이 과정을 생략하면 History를 검색할 떄 이력이 없는 것으로 동작해 무조건 Decrease를 호출하게 되기 때문 )
        commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("There is no comment id '" + commentId + "'"));

        FBCLikeHistory history = commentHistoryRepository.findHistory(memberId, commentId, true)
                .orElse(null);

        if (history == null) {
            FBCLikeHistory newHistory = FBCLikeHistory.createNewFBCLikeHistory(commentId, memberId, true);
            commentHistoryRepository.save(newHistory);
            commentRepository.increaseTotalLike(commentId);
            return "INCREASE";
        }else{
            commentHistoryRepository.delete(history);
            commentRepository.decreaseTotalLike(commentId);
            return "DECREASE";
        }
    }

    public String unlikeComment(Long commentId, Long memberId) {
        // 댓글이 있는 지 없는 지 확인 해야 함 ( 이 과정을 생략하면 History를 검색할 떄 이력이 없는 것으로 동작해 무조건 Decrease를 호출하게 되기 때문 )
        commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("There is no comment id '" + commentId + "'"));

        FBCLikeHistory history = commentHistoryRepository.findHistory(memberId, commentId, false)
                .orElse(null);

        if (history == null) {
            FBCLikeHistory newHistory = FBCLikeHistory.createNewFBCLikeHistory(commentId, memberId, false);
            commentHistoryRepository.save(newHistory);
            commentRepository.increaseTotalUnlike(commentId);
            return "INCREASE";
        }else{
            commentHistoryRepository.delete(history);
            commentRepository.decreaseTotalUnlike(commentId);
            return "DECREASE";
        }
    }



}
