package com.comiclub.web.contoller.admin;


import com.comiclub.domain.entity.board.Club;
import com.comiclub.domain.entity.board.FreeBoard;
import com.comiclub.domain.entity.board.FreeWorkBoard;
import com.comiclub.domain.entity.sereis.Series;
import com.comiclub.domain.repository.board.freework.FWBoardRepository;
import com.comiclub.domain.repository.club.ClubRepository;
import com.comiclub.domain.repository.series.SeriesRepository;
import com.comiclub.domain.service.admin.AdminService;
import com.comiclub.web.contoller.board.freework.dto.FreeWorkBoardSearchCond;
import com.comiclub.web.contoller.board.series.dto.SeriesSearchCond;
import com.comiclub.web.contoller.club.BoardSearchCond;
import com.comiclub.web.contoller.club.FreeBoardSearchType;
import com.comiclub.web.contoller.club.dto.FreeBoardItemDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/")
public class AdminController {


    private final AdminService adminService;

    private final ClubRepository clubRepository;
    private final SeriesRepository seriesRepository;
    private final FWBoardRepository fwBoardRepository;

    @GetMapping("board/free")
    public String freeBoard(@RequestParam(value = "cid", defaultValue = "1") Long clubId,
                            @RequestParam(value = "type", required = false) FreeBoardSearchType type,
                            @RequestParam(value = "keyword", defaultValue = "") String keyword,
                            @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable,
                            Model model){

        BoardSearchCond boardSearchCond = new BoardSearchCond();
        boardSearchCond.setViewAdult(true);
        if(type != null) boardSearchCond.setType(type);
        boardSearchCond.setKeyword(keyword);

        Page<FreeBoard> freeBoardPage = adminService.searchFreeBoard(clubId, boardSearchCond, pageable);
        List<FreeBoardItemDto> result = freeBoardPage.get()
                .map(board -> new FreeBoardItemDto(board))
                .collect(Collectors.toList());


        @Data
        class ClubNameDto{
            private Long id;
            private String name;

            public ClubNameDto(Club club) {
                this.id = club.getId();
                this.name = club.getClubName();
            }
        }

        List<ClubNameDto> clubs = clubRepository.findAll().stream()
                .map(club -> new ClubNameDto(club))
                .collect(Collectors.toList());

        model.addAttribute("page", PageableExecutionUtils.getPage(result, pageable, () -> freeBoardPage.getTotalElements()));
        model.addAttribute("clubs", clubs);
        model.addAttribute("clubId", clubId);
        return "views/admin/free_board";
    }

    @GetMapping("work/series")
    public String freeBoard(@ModelAttribute SeriesSearchCond cond,
                            @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable,
                            Model model){

        @Data
        class SeriesMinInfo{
            private Long id;
            private String name;
            private String writer;
            private boolean adultOnly;
            private boolean pub;
            private LocalDateTime createdDate;

            public SeriesMinInfo(Series series) {
                this.id = series.getId();
                this.name = series.getName();
                this.writer = series.getWriter();
                this.adultOnly = series.getAdultOnly();
                this.pub = series.getPub();
                this.createdDate = series.getCreatedDate();
            }
        }

        Page<Series> seriesPage = seriesRepository.adminSearchSeries(cond, pageable);
        List<SeriesMinInfo> result = seriesPage.get()
                .map(series -> new SeriesMinInfo(series))
                .collect(Collectors.toList());

        model.addAttribute("page", PageableExecutionUtils.getPage(result, pageable, () -> seriesPage.getTotalElements()));
        return "views/admin/series";
    }

    @GetMapping("work/free")
    public String freeBoard(@ModelAttribute FreeWorkBoardSearchCond cond,
                            @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable,
                            Model model){

        @Data
        class FreeWorkBoardMinInfo {
            private Long id;
            private String title;
            private String writer;
            private boolean adultOnly;
            private boolean pub;
            private LocalDateTime createdDate;

            public FreeWorkBoardMinInfo(FreeWorkBoard freeWorkBoard) {
                this.id = freeWorkBoard.getId();
                this.title = freeWorkBoard.getTitle();
                this.writer = freeWorkBoard.getWriter();
                this.adultOnly = freeWorkBoard.getAdultOnly();
                this.pub = freeWorkBoard.getPub();
                this.createdDate = freeWorkBoard.getCreatedDate();
            }
        }

        Page<FreeWorkBoard> freeWorkBoardPage = fwBoardRepository.adminSearchFreeWorkBoard(cond, pageable);
        List<FreeWorkBoardMinInfo> result = freeWorkBoardPage.get()
                .map(workBoard -> new FreeWorkBoardMinInfo(workBoard))
                .collect(Collectors.toList());

        model.addAttribute("page", PageableExecutionUtils.getPage(result, pageable, () -> freeWorkBoardPage.getTotalElements()));
        return "views/admin/free_work_board";
    }
}
