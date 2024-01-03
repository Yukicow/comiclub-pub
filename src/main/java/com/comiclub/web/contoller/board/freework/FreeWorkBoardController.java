package com.comiclub.web.contoller.board.freework;

import com.comiclub.domain.entity.freework.FreeWork;
import com.comiclub.domain.entity.member.Role;
import com.comiclub.domain.repository.board.freework.FWBLikeHistoryRepository;
import com.comiclub.domain.repository.freework.FreeWorkRepository;
import com.comiclub.web.contoller.common.dto.CommentDto;
import com.comiclub.web.contoller.board.*;
import com.comiclub.web.contoller.board.dto.RealTimeBestDto;
import com.comiclub.web.contoller.board.dto.ViewerBoardDto;
import com.comiclub.web.contoller.board.dto.FreeWorkBoardInfo;
import com.comiclub.domain.entity.board.FreeWorkBoard;
import com.comiclub.domain.repository.board.freework.FWBoardRepository;
import com.comiclub.domain.service.board.FWBoardService;
import com.comiclub.web.contoller.board.freework.dto.FreeWorkBoardSearchCond;
import com.comiclub.web.contoller.board.freework.dto.ViewerFreeWorkDto;
import com.comiclub.web.exception.*;
import com.comiclub.web.security.CurrentMember;
import com.comiclub.web.security.LoginMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/fwBoards")
@RequiredArgsConstructor
public class FreeWorkBoardController {

    private final FWBoardService boardService;

    private final FreeWorkRepository workRepository;
    private final FWBoardRepository boardRepository;
    private final FWBLikeHistoryRepository boardLikeHistoryRepository;

    @GetMapping("")
    public String freeWorkBoard(@CurrentMember LoginMember member,
                                @ModelAttribute("condition") FreeWorkBoardSearchCond cond, Pageable pageable, Model model){

        if(cond.getType() == null) cond.setType(FreeWorkSearchType.NONE);
        if(cond.getOrder() == null) cond.setOrder(OrderType.LATEST);
        if(member != null) cond.setViewAdult(member.getViewAdult());

        Page<FreeWorkBoardInfo> postPage = boardService.searchFreeWorkBoard(cond, pageable);
        List<RealTimeBestDto> bestFreeWorkBoards = RealTimeBestDto.REAL_TIME_BEST_FREE_WORK;
        List<RealTimeBestDto> bestSeries = RealTimeBestDto.REAL_TIME_BEST_SERIES;

        model.addAttribute("page", postPage);
        model.addAttribute("bestFreeWorkBoards", bestFreeWorkBoards);
        model.addAttribute("bestSeries", bestSeries);
        return "views/board/free_work_board";
    }


    @GetMapping("/{boardId}")
    public String freeWorkViewer(@CurrentMember LoginMember memberInfo,
                                @PathVariable Long boardId, Model model) {

        FreeWorkBoard board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("There is no freeWorkBoard id '" + boardId + "'"));

        // 비공개 상태인 경우
        boolean isAdmin = memberInfo != null && memberInfo.getRole().equals(Role.ADMIN) ? true : false;
        if(!isAdmin && !board.getPub()) {
            throw new PrivateStateException("The work is not public");
        }

        // 성인 작품인 경우
        if(board.getAdultOnly()) {
            if(memberInfo == null || !memberInfo.getViewAdult())
                throw new AdultOnlyAccessibleException("Need adult certification");
        }

        Long workId = board.getFreeWorkId();
        FreeWork freeWork = workRepository.findWithScenesById(workId)
                .orElseThrow(() -> new NotFoundException("There is no freeWork id '" + workId + "'"));

        boardService.increaseTotalView(boardId);

        ViewerFreeWorkDto viewerFreeWorkDto = new ViewerFreeWorkDto(freeWork);

        Pageable pageable = PageRequest.of(0, 15, Sort.by("createdDate").descending());
        Page<CommentDto> commentsPage = boardService.getPageOfMoreComments(boardId, pageable);

        ViewerBoardDto viewerBoardDto = new ViewerBoardDto(board);

        boolean likeBoard = memberInfo == null
                ? false
                : boardLikeHistoryRepository.existsByBoardIdAndMemberId(boardId, memberInfo.getId());

        model.addAttribute("work", viewerFreeWorkDto);
        model.addAttribute("board", viewerBoardDto);
        model.addAttribute("page", commentsPage);
        model.addAttribute("likeBoard", likeBoard);
        return "views/board/detail/free_work_viewer";
    }


}
