package com.comiclub.domain.service.board;


import com.comiclub.domain.entity.freework.FreeWork;
import com.comiclub.domain.entity.board.FreeWorkBoard;
import com.comiclub.domain.entity.board.FreeWorkBoardComment;
import com.comiclub.domain.entity.histroy.FWBCLikeHistory;
import com.comiclub.domain.entity.histroy.FWBLikeHistory;
import com.comiclub.domain.repository.board.freework.FWBCLikeHistoryRepository;
import com.comiclub.domain.repository.board.freework.FWBCommentRepository;
import com.comiclub.domain.repository.board.freework.FWBLikeHistoryRepository;
import com.comiclub.domain.repository.board.freework.FWBoardRepository;
import com.comiclub.domain.repository.freework.FreeWorkRepository;
import com.comiclub.web.contoller.board.dto.FreeWorkBoardInfo;
import com.comiclub.web.contoller.board.freework.dto.FreeWorkBoardSearchCond;
import com.comiclub.web.contoller.board.form.WorkBoardSaveForm;
import com.comiclub.web.contoller.common.dto.CommentDto;
import com.comiclub.web.contoller.common.dto.CommentSaveDto;
import com.comiclub.web.contoller.common.dto.DecreaseInfo;
import com.comiclub.web.exception.NoAuthorizationException;
import com.comiclub.web.exception.NotFoundException;
import jakarta.validation.ValidationException;
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
@RequiredArgsConstructor
@Transactional
public class FWBoardService {


    private final FWBoardRepository boardRepository;
    private final FWBCommentRepository commentRepository;
    private final FWBLikeHistoryRepository boardLikeHistoryRepository;
    private final FWBCLikeHistoryRepository commentLikeHistoryRepository;
    private final FreeWorkRepository freeWorkRepository;



    /**
     * Board
     * */



    @Transactional(readOnly = true)
    public Page<FreeWorkBoardInfo> searchFreeWorkBoard(FreeWorkBoardSearchCond cond, Pageable pageable) {

        Page<FreeWorkBoard> page = boardRepository.searchFreeWorkBoard(cond, pageable);

        List<FreeWorkBoardInfo> posts = page.stream()
                .map(workBoard -> new FreeWorkBoardInfo(workBoard))
                .collect(Collectors.toList());

        return PageableExecutionUtils.getPage(posts, pageable, () -> page.getTotalElements());
    }



    public void saveWorkBoard(WorkBoardSaveForm form, Long workId, Long memberId, String writer) {

        FreeWork freeWork = freeWorkRepository.findWithScenesById(workId)
                .orElseThrow(() -> new NotFoundException("There is no work with id '" + workId + "'"));

        if (!freeWork.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for removing board");

        FreeWorkBoard board = FreeWorkBoard.createNewFreeWorkBoard(
                memberId,
                workId,
                form.getTitle(),
                writer,
                freeWork.getThumbnailUrl(),
                form.getAuthorWord(),
                form.getAdultOnly(),
                form.getIsPublic()
        );

        boardRepository.save(board);
    }

    public Long deleteWorkBoard(Long boardId, Long memberId) {

        FreeWorkBoard board = boardRepository.findOneByWorkId(boardId)
                .orElseThrow(() -> new NotFoundException("There is no WorkBoard"));

        if (!board.getMemberId().equals(memberId)) 
            throw new NoAuthorizationException("No authorization for removing board");

        boardRepository.delete(board);

        return board.getFreeWorkId();
    }

    public Long updateWorkBoard(WorkBoardSaveForm form, Long workId, Long memberId) {

        FreeWorkBoard board = boardRepository.findOneByWorkId(workId)
                .orElseThrow(() -> new NotFoundException("There is no WorkBoard"));

        if (!board.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for updating board");

        board.changeTitle(form.getTitle());
        board.changeAuthorWords(form.getAuthorWord());
        board.changeAdultOnly(form.getAdultOnly());
        board.changePub(form.getIsPublic());

        return board.getFreeWorkId();
    }

    public void likeBoard(Long boardId, Long memberId) {
        // 게시물이 있는 지 없는 지 확인 해야 함 ( 이 과정을 생략하면 History를 검색할 떄 이력이 없는 것으로 동작해 무조건 Decrease를 호출하게 되기 때문 )
        boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("There is no board id '" + boardId + "'"));

        FWBLikeHistory history = boardLikeHistoryRepository.findHistory(boardId, memberId)
                .orElse(null);

        if(history == null){
            boardLikeHistoryRepository.save(history);
            boardRepository.increaseTotalLike(history.getBoardId());
        }
        else{
            boardLikeHistoryRepository.delete(history);
            boardRepository.decreaseTotalLike(history.getBoardId(), 1);
        }
    }




    /**
     * Comment
     * */

    @Transactional(readOnly = true)
    public Slice<CommentDto> getSliceOfMoreComments(Long boardId, Long motherCommentId, Pageable pageable) {

        FreeWorkBoard board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("There is no board with id '" + boardId + "'"));

        if(!board.getPub())
            throw new NoAuthorizationException("The board is forbidden");

        Slice<FreeWorkBoardComment> replies = commentRepository.findSliceOfMoreComments(boardId, motherCommentId, pageable);

        List<CommentDto> content = replies.stream()
                .map(comment -> new CommentDto(comment))
                .collect(Collectors.toList());

        return new SliceImpl<>(content, pageable, replies.hasNext());
    }

    @Transactional(readOnly = true)
    public Page<CommentDto> getPageOfMoreComments(Long boardId, Pageable pageable) {

        FreeWorkBoard board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("There is no board with id '" + boardId + "'"));

        if(!board.getPub())
            throw new NoAuthorizationException("The board is forbidden");

        Page<FreeWorkBoardComment> replies = commentRepository.findPageOfMoreComments(boardId, pageable);

        List<CommentDto> content = replies.stream()
                .map(comment -> new CommentDto(comment))
                .collect(Collectors.toList());

        return PageableExecutionUtils.getPage(content, pageable, () -> replies.getTotalPages());
    }

    public CommentDto saveComment(CommentSaveDto saveDto) {
        FreeWorkBoardComment comment = FreeWorkBoardComment.createNewFreeWorkBoardComment(saveDto);
        commentRepository.save(comment);
        boardRepository.increaseTotalComment(comment.getBoardId());
        return new CommentDto(comment);
    }

    public CommentDto saveReply(CommentSaveDto saveDto) {
        FreeWorkBoardComment comment = FreeWorkBoardComment.createNewFreeWorkBoardComment(saveDto);
        commentRepository.save(comment);
        commentRepository.increaseTotalReply(comment.getMotherCommentId());
        boardRepository.increaseTotalComment(comment.getBoardId());
        return new CommentDto(comment);
    }

    public CommentDto updateComment(Long commentId, Long memberId, String content) {

        FreeWorkBoardComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("There is no comment with id '" + commentId + "'"));

        if(!comment.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No Authorization for updating comment");

        comment.changeContent(content);

        return new CommentDto(comment);
    }

    public DecreaseInfo deleteComment(Long commentId, Long memberId) {

        FreeWorkBoardComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("There is no comment with id '" + commentId + "'"));

        if(!comment.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No Authorization for updating comment");

        if(comment.getMotherCommentId() == null){
            boardRepository.decreaseTotalComment(comment.getBoardId(), comment.getTotalReply() + 1L);
            commentRepository.delete(comment);
            return new DecreaseInfo(comment.getMotherCommentId(), comment.getTotalReply() + 1L);
        }
        else{
            commentRepository.delete(comment);
            commentRepository.decreaseTotalReply(comment.getMotherCommentId());
            boardRepository.decreaseTotalComment(comment.getBoardId(), 1L);
            return new DecreaseInfo(comment.getMotherCommentId(), 1L);
        }
    }


    public String likeComment(Long commentId, Long memberId) {
        // 댓글이 있는 지 없는 지 확인 해야 함 ( 이 과정을 생략하면 History를 검색할 떄 이력이 없는 것으로 동작해 무조건 Decrease를 호출하게 되기 때문 )
        commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("There is no comment with commentId '" + commentId + "'"));

        FWBCLikeHistory likeHistory = commentLikeHistoryRepository.findHistory(commentId, memberId, true)
                .orElse(null);

        if(likeHistory == null){
            FWBCLikeHistory newHistory = FWBCLikeHistory.createNewFWBCLikeHistory(commentId, memberId, true);
            commentLikeHistoryRepository.save(newHistory);
            commentRepository.increaseTotalLike(commentId);
            return "INCREASE";
        }else{
            commentLikeHistoryRepository.delete(likeHistory);
            commentRepository.decreaseTotalLike(commentId);
            return "DECREASE";
        }
    }

    public String unlikeComment(Long commentId, Long memberId) {
        // 댓글이 있는 지 없는 지 확인 해야 함 ( 이 과정을 생략하면 History를 검색할 떄 이력이 없는 것으로 동작해 무조건 Decrease를 호출하게 되기 때문 )
        commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("There is no comment with commentId '" + commentId + "'"));

        FWBCLikeHistory likeHistory = commentLikeHistoryRepository.findHistory(commentId, memberId, false)
                .orElse(null);

        if(likeHistory == null){
            FWBCLikeHistory newHistory = FWBCLikeHistory.createNewFWBCLikeHistory(commentId, memberId, false);
            commentLikeHistoryRepository.save(newHistory);
            commentRepository.increaseTotalUnlike(commentId);
            return "INCREASE";
        }else{
            commentLikeHistoryRepository.delete(likeHistory);
            commentRepository.decreaseTotalUnlike(commentId);
            return "DECREASE";
        }
    }

    public void changePub(FreeWorkBoard freeWorkBoard) {
        freeWorkBoard.changePub(freeWorkBoard.getPub() ? false : true);
    }

    public void increaseTotalView(Long id) {
        boardRepository.increaseTotalView(id);
    }


}
