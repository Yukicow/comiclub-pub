package com.comiclub.web.contoller.club;

import com.comiclub.domain.entity.board.*;
import com.comiclub.domain.entity.board.enumtype.Genre;
import com.comiclub.domain.repository.board.free.FreeBoardRepository;
import com.comiclub.domain.repository.board.free.FBCommentRepository;
import com.comiclub.domain.service.board.FreeBoardService;
import com.comiclub.domain.service.club.ClubService;
import com.comiclub.web.contoller.board.dto.RealTimeBestDto;
import com.comiclub.web.contoller.club.dto.ClubDto;
import com.comiclub.web.contoller.club.dto.FreeBoardDetailDto;
import com.comiclub.web.contoller.club.dto.FreeBoardItemDto;
import com.comiclub.web.contoller.club.form.FreeBoardForm;
import com.comiclub.web.exception.AdultOnlyAccessibleException;
import com.comiclub.web.exception.NoAuthenticationException;
import com.comiclub.web.exception.NoAuthorizationException;
import com.comiclub.web.exception.NotFoundException;
import com.comiclub.web.security.CurrentMember;
import com.comiclub.web.security.LoginMember;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/clubs")
@RequiredArgsConstructor
public class ClubController {


    private final ClubService clubService;

    private final FreeBoardService freeBoardService;
    private final FreeBoardRepository freeBoardRepository;
    private final FBCommentRepository fbCommentRepository;


    public Club setClubInfo(Long clubId, Model model){
        Club club = clubService.findById(clubId).orElseThrow(NotFoundException::new);
        ClubDto clubInfo = new ClubDto(club);

        model.addAttribute("club", clubInfo);
        return club;
    }




    @GetMapping("")
    public String viewClub(Model model) {
        List<Club> clubs = clubService.findAll();
        List<ClubDto> result = clubs.stream()
                .sorted(Comparator.comparing(Club::getClubName))
                .map(club -> new ClubDto(club))
                .collect(Collectors.toList());

        // 장르의 name값을 Key로 하고 Club들을 담은 List를 Value로 하는 Map을 생성 후 장르에 따라 분류에 맞는 Club 정보를 넣음
        Map<String, List<ClubDto>> map = new HashMap<>();
        map.put(Genre.GAME.name(), new ArrayList<>());
        map.put(Genre.CARTOON.name(), new ArrayList<>());
        map.put(Genre.ANIMATION.name(), new ArrayList<>());
        map.put(Genre.OTHER.name(), new ArrayList<>());

        result.forEach(club -> {  // 장르에 따라서 구분하여 저장
            switch (club.getGenre()){
                case GAME:
                    map.get(Genre.GAME.name()).add(club);
                    break;
                case CARTOON:
                    map.get(Genre.CARTOON.name()).add(club);
                    break;
                case ANIMATION:
                    map.get(Genre.ANIMATION.name()).add(club);
                    break;
                case OTHER:
                    map.get(Genre.OTHER.name()).add(club);
                    break;
            }
        });

        model.addAttribute("map", map);
        return "views/club/club_list";
    }


    /**
     * Account쓸 건지 따로 클래스 만들어서 슬 건지 정하기
     * */
    @GetMapping("/{clubId}")
    public String freeBoard(@PathVariable Long clubId, @ModelAttribute(name = "condition") BoardSearchCond condition,
                            @CurrentMember LoginMember memberInfo, @PageableDefault(size = 15) Pageable pageable, Model model){
        setClubInfo(clubId, model);

        if(memberInfo != null)
            condition.setViewAdult(memberInfo.getViewAdult());

        Page<FreeBoard> boardResult = null;
        try { // pageNumber가 최대 페이지 보다 큰 경우 countQuery호출 후 PageImpl객체를 생성할 떄 NPE발생. 이런 경우 404를 내려줌.
            boardResult = clubService.searchBoard(clubId, condition, pageable);
        }catch (NullPointerException e){
            throw new NotFoundException("There is no board with page '" + ( pageable.getPageNumber() + 1 ) + "'");
        }

        List<FreeBoardItemDto> boards = boardResult.getContent().stream()
                .map(board -> new FreeBoardItemDto(board))
                .collect(Collectors.toList());

        List<FreeBoard> noticeResult = clubService.searchNotice(clubId, condition.getViewAdult());
        List<FreeBoardItemDto> notices = noticeResult.stream()
                .map(notice -> new FreeBoardItemDto(notice))
                .collect(Collectors.toList());

        List<RealTimeBestDto> bestEpisodeBoards = RealTimeBestDto.REAL_TIME_BEST_SERIES;
        List<RealTimeBestDto> bestFreeWorkBoards = RealTimeBestDto.REAL_TIME_BEST_FREE_WORK;

        model.addAttribute("page", PageableExecutionUtils.getPage(boards, pageable, boardResult::getTotalElements));
        model.addAttribute("notices", notices);
        model.addAttribute("bestFreeWorkBoards", bestFreeWorkBoards);
        model.addAttribute("bestSeries", bestEpisodeBoards);
        return "views/club/board";
    }

    @GetMapping("/{clubId}/board")
    public String freeBoardWriteForm(@PathVariable Long clubId, Model model){
        setClubInfo(clubId, model);
        return "views/club/write_board_form";
    }

    @GetMapping("/{clubId}/boards/{boardId}")
    public String freeBoardDetail(@CurrentMember LoginMember memberInfo, @PathVariable Long clubId, @PathVariable Long boardId, Model model,
                                  @PageableDefault(size = 15, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable){

        setClubInfo(clubId, model);

        freeBoardService.increaseTotalView(boardId);

        // board 찾기
        FreeBoard board = freeBoardRepository.findByIdAndClubId(boardId, clubId)
                .orElseThrow(() -> new NotFoundException("There is no board id '" + boardId + "'"));

        // 성인 작품인 경우
        if(board.getAdultOnly()) {
            if(memberInfo == null || !memberInfo.getViewAdult()) throw new AdultOnlyAccessibleException("Need adult certification");
        }

        FreeBoardDetailDto result = new FreeBoardDetailDto(board);

        // board에서 제읾 먼저 보여질 최상위 comments 찾기(motherBoard가 null인 댓글들)
        Page<FreeBoardComment> commentPage = fbCommentRepository.findPageOfMoreComments(boardId, pageable);

        model.addAttribute("board", result);
        model.addAttribute("page", commentPage);
        return "views/club/board_detail";
    }

    @GetMapping("/{clubId}/boards/{boardId}/info")
    public String editFreeBoardForm(@PathVariable Long clubId, @PathVariable Long boardId, Model model, @CurrentMember LoginMember memberInfo, HttpServletRequest request){

        setClubInfo(clubId, model);

        // board 찾기
        FreeBoard freeBoard = freeBoardRepository.findByIdAndClubId(boardId, clubId)
                .orElseThrow(() -> new NotFoundException("There is no board id '" + boardId + "'"));

        if(!freeBoard.getClubId().equals(clubId))
            throw new IllegalArgumentException("Club id does not match");

        boolean authMember = memberInfo != null; // 인증된 사용자인지
        boolean authWriter = freeBoard.getMemberId() != null; // 게시글이 인증된 사용자가 등록한 게시글인지

        if(authWriter && authMember){ // 인증된 사용자가 등록한 게시글이면서 로그인 유저인 경우
            boolean sameWriter = freeBoard.getMemberId().equals(memberInfo.getId());
            if(!sameWriter)
                throw new NoAuthorizationException("No authorization for modifying board");
        }
        else{
            if(request.getCookies() == null)
                throw new NoAuthorizationException("No authorization for modifying board");

            Cookie passwordCookie = Arrays.stream(request.getCookies())
                    .filter((cookie) -> cookie.getName().equals("FB" + boardId))
                    .findFirst()
                    .orElseThrow(() -> new NoAuthorizationException("No authorization for modifying board"));

            boolean matchPassword = freeBoard.getPassword().equals(passwordCookie.getValue()); // password가 일치하는지
            if(!matchPassword)
                throw new NoAuthorizationException("No authorization for modifying board");
        }

        FreeBoardForm freeBoardForm = new FreeBoardForm(freeBoard);

        model.addAttribute("board", freeBoardForm);
        model.addAttribute("boardId", freeBoard.getId());
        return "views/club/edit_board_form";
    }





}
