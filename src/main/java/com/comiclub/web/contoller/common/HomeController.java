package com.comiclub.web.contoller.common;


import com.comiclub.domain.entity.member.Member;
import com.comiclub.domain.entity.member.Role;
import com.comiclub.web.result.ErrorResult;
import com.comiclub.web.contoller.board.dto.FreeWorkBoardInfo;
import com.comiclub.web.contoller.board.series.dto.SeriesBoardDto;
import com.comiclub.web.contoller.common.form.JoinForm;
import com.comiclub.web.exception.NoAuthenticationException;
import com.comiclub.web.exception.NoAuthorizationException;
import com.comiclub.web.exception.NoSuchLoginInfoException;
import com.comiclub.domain.service.member.MemberService;
import com.comiclub.web.security.CurrentMember;
import com.comiclub.web.security.LoginMember;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MemberService memberService;


    private final PasswordEncoder passwordEncoder;


    private static List<FreeWorkBoardInfo> weekBestFreeWorks = new ArrayList<>();
    private static List<SeriesBoardDto> weekBestSeries = new ArrayList<>();
    private static List<FreeWorkBoardInfo> freeWorkRanks = new ArrayList<>();
    private static List<SeriesBoardDto> seriesRanks = new ArrayList<>();

    public static void setWeekBestFreeWorks(List<FreeWorkBoardInfo> weekBestFreeWorks) {
        HomeController.weekBestFreeWorks = weekBestFreeWorks;
    }

    public static void setWeekBestSeries(List<SeriesBoardDto> weekBestSeries) {
        HomeController.weekBestSeries = weekBestSeries;
    }

    public static void setFreeWorkRanks(List<FreeWorkBoardInfo> freeWorkRanks) {
        HomeController.freeWorkRanks = freeWorkRanks;
    }

    public static void setSeriesRanks(List<SeriesBoardDto> seriesRanks) {
        HomeController.seriesRanks = seriesRanks;
    }




    @ResponseBody
    @ExceptionHandler
    public ResponseEntity<ErrorResult> loginFailHandler(NoSuchLoginInfoException e) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResult(HttpStatus.BAD_REQUEST.value(),"FAIL", e.getMessage()));
    }


    @GetMapping("/")
    public String index(Model model){
        model.addAttribute("weekBestSeries", weekBestSeries);
        model.addAttribute("weekBestFreeWorks", weekBestFreeWorks);
        model.addAttribute("seriesRanks", seriesRanks);
        model.addAttribute("freeWorkRanks", freeWorkRanks);
        return "views/index";
    }

    @GetMapping("/login")
    public String login(@CurrentMember LoginMember memberInfo, HttpServletRequest request){

        if(memberInfo != null){
            throw new NoAuthorizationException("Already login state");
        }

        String uri = request.getHeader("Referer");
        if (uri != null && !uri.contains("/login")) {
            request.getSession().setAttribute("prevPage", uri);
        }
        return "views/login";
    }


    @GetMapping("/join")
    public String join(){
        return "views/join";
    }

    @ResponseBody
    @PostMapping("/join")
    public ResponseEntity<Object> join(@Validated @ModelAttribute("member") JoinForm form,
                                       BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Validation error has been occurred");
        }

        Member member = Member.createNewMember(form, passwordEncoder, Role.MEMBER);
        memberService.join(member);

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    @GetMapping("/member/adult")
    public ResponseEntity<Object> authAdult(@CurrentMember LoginMember memberInfo) {

        if(!memberInfo.getAdult()){
            throw new NoAuthenticationException("No authentication for opening adultOnly");
        }

        return ResponseEntity.badRequest().build();
    }


}
