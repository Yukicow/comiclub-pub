package com.comiclub.web.contoller.member;


import com.comiclub.domain.repository.series.FollowHistoryRepository;
import com.comiclub.domain.repository.series.SeriesRepository;
import com.comiclub.web.result.CommonResult;
import com.comiclub.domain.service.member.MemberService;
import com.comiclub.web.exception.AdultOnlyAccessibleException;
import com.comiclub.web.security.CurrentMember;
import com.comiclub.web.security.LoginMember;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberRestController {


    private final MemberService memberService;
    private final FollowHistoryRepository followHistoryRepository;
    private final SeriesRepository seriesRepository;


    @GetMapping("/follow-list")
    public ResponseEntity<CommonResult> followList(@CurrentMember LoginMember memberInfo,
                             @RequestParam("offset") int offset){

        PageRequest pageRequest = PageRequest.of(offset, 20);
        List<Long> ids = followHistoryRepository.findFollowList(memberInfo.getId(), memberInfo.getViewAdult(), pageRequest).stream()
                .map(history -> history.getSeriesId())
                .collect(Collectors.toList());

        List<FollowSeriesInfo> series = seriesRepository.findAllById(ids).stream()
                .map(item -> new FollowSeriesInfo(item))
                .collect(Collectors.toList());

        return ResponseEntity
                .ok()
                .body(new CommonResult<>(HttpServletResponse.SC_OK, "SUCCESS", series));
    }

    @PatchMapping("/view-adult")
    public ResponseEntity<CommonResult> changeViewAdult(@CurrentMember LoginMember memberInfo, HttpServletRequest request) {

        if(memberInfo == null || !memberInfo.getAdult()) throw new AdultOnlyAccessibleException("Need adult certification");

        boolean viewAdult = memberInfo.getViewAdult() ? false : true;
        memberService.changeViewAdult(memberInfo.getId(), viewAdult);

        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "REDIRECT", request.getHeader("REFERER")));
    }



}
