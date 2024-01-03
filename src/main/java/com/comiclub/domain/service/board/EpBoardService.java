package com.comiclub.domain.service.board;


import com.comiclub.domain.entity.board.EpisodeBoard;
import com.comiclub.domain.entity.board.EpisodeBoardComment;
import com.comiclub.domain.entity.histroy.EBCLikeHistory;
import com.comiclub.domain.entity.histroy.EBLikeHistory;
import com.comiclub.domain.entity.sereis.Episode;
import com.comiclub.domain.repository.board.episode.EBCLikeHistoryRepository;
import com.comiclub.domain.repository.board.episode.EBCommentRepository;
import com.comiclub.domain.repository.board.episode.EBLikeHistoryRepository;
import com.comiclub.domain.repository.board.episode.EBoardRepository;
import com.comiclub.domain.repository.series.EpisodeRepository;
import com.comiclub.domain.repository.series.SeriesRepository;
import com.comiclub.web.contoller.common.dto.CommentDto;
import com.comiclub.web.contoller.common.dto.CommentSaveDto;
import com.comiclub.web.contoller.board.form.WorkBoardSaveForm;
import com.comiclub.web.contoller.common.dto.DecreaseInfo;
import com.comiclub.web.exception.NoAuthorizationException;
import com.comiclub.web.exception.NotFoundException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
public class EpBoardService {

    private final SeriesRepository seriesRepository;
    private final EBoardRepository boardRepository;
    private final EpisodeRepository episodeRepository;
    private final EBCommentRepository commentRepository;
    private final EBLikeHistoryRepository boardLikeHistoryRepository;
    private final EBCLikeHistoryRepository commentLikeHistoryRepository;




    /**
     * Board
     * */


    public void saveEpisodeBoard(WorkBoardSaveForm form, Long episodeId, Long memberId, String writer) {

        Episode episode = episodeRepository.findWithScenesById(episodeId)
                .orElseThrow(() -> new NotFoundException("There is no work id '" + episodeId + "'"));

        if (!episode.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for uploading board");

        EpisodeBoard board = EpisodeBoard.createNewEpisodeBoard(
                memberId,
                episode.getSeriesId(),
                episodeId,
                episode.getEpisodeNumber(),
                form.getTitle(),
                writer,
                episode.getThumbnailUrl(),
                form.getAuthorWord(),
                form.getAdultOnly(),
                form.getIsPublic()
        );

        if(board.getPub()){
            seriesRepository.increaseTotalEpBoard(board.getSeriesId());
        }

        boardRepository.save(board);
    }

    public void deleteEpisodeBoard(Long boardId, Long memberId) {

        EpisodeBoard board = boardRepository.findOneByEpisodeId(boardId)
                .orElseThrow(() -> new NotFoundException("There is no WorkBoard id'" + boardId + "'"));

        if (!board.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for updating board");

        if(board.getPub()){
            seriesRepository.decreaseTotalEpBoard(board.getSeriesId());
        }

        boardRepository.delete(board);
    }

    public void updateEpisodeBoard(WorkBoardSaveForm form, Long episodeId, Long memberId) {

        EpisodeBoard board = boardRepository.findOneByEpisodeId(episodeId)
                .orElseThrow(() -> new NotFoundException("There is no EpisodeBoard"));

        if (!board.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for updating board");

        board.changeTitle(form.getTitle());
        board.changeAuthorWords(form.getAuthorWord());
        board.changeAdultOnly(form.getAdultOnly());
        board.changePub(form.getIsPublic());

        if(form.getIsPublic() != board.getPub()){

            board.changePub(form.getIsPublic());

            if(form.getIsPublic()){
                seriesRepository.increaseTotalEpBoard(board.getSeriesId());
            }
            else{
                seriesRepository.decreaseTotalEpBoard(board.getSeriesId());
            }

        }

    }


    public String likeBoard(Long boardId, Long memberId) {
        // 게시물이 있는 지 없는 지 확인 해야 함 ( 이 과정을 생략하면 History를 검색할 떄 이력이 없는 것으로 동작해 무조건 Decrease를 호출하게 되기 때문 )
        EpisodeBoard board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("There is no board id '" + boardId + "'"));

        EBLikeHistory history = boardLikeHistoryRepository.findHistory(boardId, memberId)
                .orElse(null);

        if(history == null){
            EBLikeHistory newHistory = EBLikeHistory.createNewEBLikeHistory(boardId, memberId);
            boardLikeHistoryRepository.save(newHistory);
            boardRepository.increaseTotalLike(boardId);
            seriesRepository.increaseTotalLike(board.getSeriesId());
            return "INCREASE";
        }
        else{
            boardLikeHistoryRepository.delete(history);
            boardRepository.decreaseTotalLike(boardId);
            seriesRepository.decreaseTotalLike(board.getSeriesId());
            return "DECREASE";
        }

    }




    /**
     * Comment
     * */

    @Transactional(readOnly = true)
    public Slice<CommentDto> getSliceOfMoreComments(Long boardId, Long motherCommentId, Pageable pageable) {

        EpisodeBoard board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("There is no board id '" + boardId + "'"));

        if(!board.getPub())
            throw new NoAuthorizationException("The board is forbidden");

        Slice<EpisodeBoardComment> slice = commentRepository.findSliceOfMoreComments(boardId, motherCommentId, pageable);

        List<CommentDto> content = slice.stream()
                .map(comment -> new CommentDto(comment))
                .collect(Collectors.toList());

        return new SliceImpl<>(content, pageable, slice.hasNext());
    }

    @Transactional(readOnly = true)
    public Page<CommentDto> getPageOfMoreComments(Long boardId, Long motherCommentId, Pageable pageable) {

        EpisodeBoard board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("There is no board id '" + boardId + "'"));

        if(!board.getPub())
            throw new NoAuthorizationException("The board is forbidden");

        Page<EpisodeBoardComment> page = commentRepository.findPageOfMoreComments(boardId, pageable);

        List<CommentDto> content = page.stream()
                .map(comment -> new CommentDto(comment))
                .collect(Collectors.toList());

        return PageableExecutionUtils.getPage(content, pageable, () -> page.getTotalPages());
    }

    public CommentDto saveComment(CommentSaveDto dto) {

        EpisodeBoardComment comment = EpisodeBoardComment.createNewEpisodeBoardComment(dto);

        commentRepository.save(comment);
        boardRepository.increaseTotalComment(comment.getBoardId());

        return new CommentDto(comment);
    }

    public CommentDto saveReply(CommentSaveDto dto) {
        commentRepository.findByIdAndBoardId(dto.getMotherCommentId(), dto.getBoardId())
                .orElseThrow(() -> new NotFoundException("Bad Request"));

        EpisodeBoardComment reply = EpisodeBoardComment.createNewEpisodeBoardComment(dto);

        commentRepository.save(reply);

        commentRepository.increaseTotalReply(reply.getMotherCommentId());
        boardRepository.increaseTotalComment(reply.getBoardId());

        return new CommentDto(reply);
    }

    public void updateComment(Long commentId, Long memberId, String content) {

        EpisodeBoardComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("There is no comment id '" + commentId + "'"));

        if(!comment.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No Authorization for updating comment");

        comment.changeContent(content);
    }

    public DecreaseInfo deleteComment(Long commentId, Long memberId) {

        EpisodeBoardComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("There is no comment id '" + commentId + "'"));

        if(!comment.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No Authorization for updating comment");

        if (comment.getMotherCommentId() == null) {
            commentRepository.delete(comment);
            boardRepository.decreaseTotalComment(comment.getBoardId(), comment.getTotalReply() + 1L);
            return new DecreaseInfo(null, comment.getTotalReply() + 1L);
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
                .orElseThrow(() -> new NotFoundException("There is no comment id '" + commentId + "'"));

        EBCLikeHistory history = commentLikeHistoryRepository.findHistory(commentId, memberId, true)
                .orElse(null);

        if(history == null){
            EBCLikeHistory newHistory = EBCLikeHistory.createNewEBCLikeHistory(commentId, memberId, true);
            commentLikeHistoryRepository.save(newHistory);
            commentRepository.increaseTotalLike(commentId);
            return "INCREASE";
        }else{
            commentLikeHistoryRepository.delete(history);
            commentRepository.decreaseTotalLike(commentId);
            return "DECREASE";
        }
    }

    public String unlikeComment(Long commentId, Long memberId) {
        // 댓글이 있는 지 없는 지 확인 해야 함 ( 이 과정을 생략하면 History를 검색할 떄 이력이 없는 것으로 동작해 무조건 Decrease를 호출하게 되기 때문 )
        commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("There is no comment id '" + commentId + "'"));

        EBCLikeHistory history = commentLikeHistoryRepository.findHistory(commentId, memberId, false)
                .orElse(null);

        if(history == null){
            EBCLikeHistory newHistory = EBCLikeHistory.createNewEBCLikeHistory(commentId, memberId, false);
            commentLikeHistoryRepository.save(newHistory);
            commentRepository.increaseTotalUnlike(commentId);
            return "INCREASE";
        }else{
            commentLikeHistoryRepository.delete(history);
            commentRepository.decreaseTotalUnlike(commentId);
            return "DECREASE";
        }
    }


    public void authComment(Long commentId, Long memberId) {
        EpisodeBoardComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("There is no comment id '" + commentId + "'"));

        if(!comment.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No Authorization for updating comment");
    }
}
