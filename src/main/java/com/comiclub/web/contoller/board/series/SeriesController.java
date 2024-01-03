package com.comiclub.web.contoller.board.series;

import com.comiclub.domain.entity.board.EpisodeBoard;
import com.comiclub.domain.entity.member.Role;
import com.comiclub.domain.entity.sereis.Episode;
import com.comiclub.domain.entity.sereis.Series;
import com.comiclub.domain.repository.board.episode.EBLikeHistoryRepository;
import com.comiclub.domain.repository.board.episode.EBoardRepository;
import com.comiclub.domain.repository.board.freework.FWBoardRepository;
import com.comiclub.domain.repository.series.EpisodeRepository;
import com.comiclub.domain.repository.series.FollowHistoryRepository;
import com.comiclub.domain.repository.series.SeriesRepository;
import com.comiclub.domain.service.board.EpBoardService;
import com.comiclub.domain.service.board.SeriesService;
import com.comiclub.web.contoller.board.dto.EpisodeBoardInfo;
import com.comiclub.web.contoller.common.dto.CommentDto;
import com.comiclub.web.contoller.board.OrderType;
import com.comiclub.web.contoller.board.dto.RealTimeBestDto;
import com.comiclub.web.contoller.board.dto.ViewerBoardDto;
import com.comiclub.web.contoller.board.dto.FreeWorkBoardInfo;
import com.comiclub.web.contoller.board.series.dto.SeriesBoardDto;
import com.comiclub.web.contoller.board.series.dto.SeriesSearchCond;
import com.comiclub.web.contoller.board.series.dto.SeriesSideDto;
import com.comiclub.web.contoller.board.series.dto.ViewerEpisodeDto;
import com.comiclub.web.exception.*;
import com.comiclub.web.security.CurrentMember;
import com.comiclub.web.security.LoginMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/series")
@RequiredArgsConstructor
public class SeriesController {

    private final SeriesService seriesService;
    private final EpBoardService eBoardService;


    private final SeriesRepository seriesRepository;
    private final FollowHistoryRepository followHistoryRepository;
    private final FWBoardRepository fwBoardRepository;

    private final EBoardRepository eBoardRepository;

    private final EpisodeRepository episodeRepository;
    private final EBLikeHistoryRepository boardLikeHistoryRepository;




    @GetMapping("")
    public String seriesWorkBoard(@CurrentMember LoginMember memberInfo, @ModelAttribute("condition") SeriesSearchCond cond,
                                  @PageableDefault(sort = "updatedDate", direction = Sort.Direction.DESC) Pageable pageable, Model model) {

        if (cond.getType() == null) cond.setType(SeriesSearchType.NONE);
        if (cond.getOrder() == null) cond.setOrder(OrderType.LATEST);
        if (memberInfo != null) cond.setViewAdult(memberInfo.getViewAdult());

        Page<SeriesBoardDto> seriesPage = seriesService.searchSeries(cond, pageable);
        List<RealTimeBestDto> bestSeries = RealTimeBestDto.REAL_TIME_BEST_SERIES;
        List<RealTimeBestDto> bestFreeWorkBoards = RealTimeBestDto.REAL_TIME_BEST_FREE_WORK;

        model.addAttribute("page", seriesPage);
        model.addAttribute("bestFreeWorkBoards", bestFreeWorkBoards);
        model.addAttribute("bestSeries", bestSeries);
        return "views/board/series_board";
    }

    @GetMapping("/{seriesId}")
    public String seriesDetail(@CurrentMember LoginMember memberInfo, @PathVariable Long seriesId, Model model,
                                   @PageableDefault(size = 15) Pageable pageable) {

        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new NotFoundException("There is no series id with '" + seriesId + "'"));

        // 비공개 상태인 경우
        boolean isAdmin = memberInfo != null && memberInfo.getRole().equals(Role.ADMIN) ? true : false;
        if(!isAdmin && !series.getPub()) {
            throw new PrivateStateException("The series is not public");
        }

        // 성인 작품인 경우
        if (series.getAdultOnly()) {
            if(memberInfo == null || !memberInfo.getViewAdult()) throw new AdultOnlyAccessibleException("Need adult certification");
        }

        boolean adultOnly = memberInfo != null ? memberInfo.getViewAdult() : false;

        // 시리즈
        SeriesBoardDto seriesBoardDto = new SeriesBoardDto(series);

        // 에피소드 페이징
        Long memberId = series.getMemberId();
        Page<EpisodeBoard> episodeBoardPage = eBoardRepository.findPubBoardBySeriesId(series.getId(), pageable);
        List<EpisodeBoardInfo> episodes = episodeBoardPage.stream().map(workBoard -> new EpisodeBoardInfo(workBoard))
                .collect(Collectors.toList());
        Page<EpisodeBoardInfo> episodePage = PageableExecutionUtils.getPage(episodes, pageable, () -> episodeBoardPage.getTotalElements());
        
        // 작가의 자유 작품 조회
        List<FreeWorkBoardInfo> writerFreeWorks = fwBoardRepository.findWritersFreeWorkBoards(
                        memberId, adultOnly, PageRequest.of(0, 3)
                )
                .stream()
                .map(work -> new FreeWorkBoardInfo(work))
                .collect(Collectors.toList());

        // 작가의 다른 시리즈 조회
        List<SeriesSideDto> writerSeries = seriesRepository.findWriterSeries(
                        memberId, series.getId(), adultOnly,
                        PageRequest.of(0, 3)
                )
                .stream()
                .map(item -> new SeriesSideDto(item))
                .collect(Collectors.toList());

        boolean followed = memberInfo == null
                ? false
                : followHistoryRepository.existsByMemberIdAndSeriesId(memberId, seriesId);

        model.addAttribute("series", seriesBoardDto);
        model.addAttribute("page", episodePage);
        model.addAttribute("writerFreeWorkBoards", writerFreeWorks);
        model.addAttribute("writerSeries", writerSeries);
        model.addAttribute("followed", followed);
        return "views/board/detail/series_detail";
    }




    @GetMapping("/{seriesId}/epBoards")
    public String episodeViewer(@CurrentMember LoginMember memberInfo,
                                @PathVariable Long seriesId, @RequestParam("no") long episodeNumber, Model model,
                                @PageableDefault(size = 15, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {

        // EpisodeBoard의 이전화 다음화를 함께 구함
        List<EpisodeBoard> boards = eBoardRepository.findViewerEpisodeBoard(seriesId, episodeNumber);

        // 이전화 및 다음화가 있는 지 없는 지 list를 통해 확인하고 현재 화에 해당하는 board를 구함
        Integer nextEpisodeNumber = null;
        Integer prevEpisodeNumber = null;
        EpisodeBoard board = null;
        if(boards.isEmpty())
            new NotFoundException("There is no EpisodeBoard");
        else{
            for (EpisodeBoard b : boards) {
                if(b.getEpisodeNumber() > episodeNumber)
                    nextEpisodeNumber = b.getEpisodeNumber();
                else if(b.getEpisodeNumber() < episodeNumber)
                    prevEpisodeNumber = b.getEpisodeNumber();
                else board = b;
            }
        }

        Long boardId = board.getId();

        // 비공개 상태인 경우
        boolean isAdmin = memberInfo != null && memberInfo.getRole().equals(Role.ADMIN) ? true : false;
        if( !board.getPub() && !isAdmin ) {
            throw new PrivateStateException("The episode is not public");
        }

        Series series = seriesRepository.findById(board.getSeriesId())
                .orElseThrow(() -> new NotFoundException("There is no Series"));

        // 시리즈나 에피소드가 성인 작품으로 등록된 경우
        if(board.getAdultOnly() || series.getAdultOnly()) {
            if(memberInfo == null || !memberInfo.getViewAdult()) throw new AdultOnlyAccessibleException("Need adult certification");
        }

        // Episode 구하기
        Long episodeId = board.getEpisodeId();
        Episode episode = episodeRepository.findWithScenesById(episodeId)
                .orElseThrow(() -> new NotFoundException("There is no Episode id with '" + episodeId + "'"));

        // EpisodeBoard의 댓글 구하기
        Page<CommentDto> commentPage = eBoardService.getPageOfMoreComments(boardId, null, pageable);


        ViewerEpisodeDto viewerEpisodeDto = new ViewerEpisodeDto(episode);
        ViewerBoardDto viewerBoardDto = new ViewerBoardDto(board);

        boolean likeBoard = memberInfo == null
                ? false
                : boardLikeHistoryRepository.existsByBoardIdAndMemberId(boardId, memberInfo.getId());

        seriesService.increaseTotalView(seriesId, boardId);

        model.addAttribute("seriesId", seriesId);
        model.addAttribute("episode", viewerEpisodeDto);
        model.addAttribute("prevEpisodeNumber", prevEpisodeNumber);
        model.addAttribute("nextEpisodeNumber", nextEpisodeNumber);
        model.addAttribute("board", viewerBoardDto);
        model.addAttribute("page", commentPage);
        model.addAttribute("likeBoard", likeBoard);
        return "views/board/detail/episode_viewer";
    }



}
