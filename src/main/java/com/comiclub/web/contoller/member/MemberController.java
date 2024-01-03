package com.comiclub.web.contoller.member;


import com.comiclub.domain.entity.histroy.FollowHistory;
import com.comiclub.domain.repository.series.FollowHistoryRepository;
import com.comiclub.domain.repository.series.SeriesRepository;
import com.comiclub.web.security.CurrentMember;
import com.comiclub.web.security.LoginMember;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {


    private final SeriesRepository seriesRepository;
    private final FollowHistoryRepository followHistoryRepository;



    @GetMapping("/follows")
    public String followList(@CurrentMember LoginMember memberInfo, Model model){

        PageRequest pageRequest = PageRequest.of(0, 8);
        Page<FollowHistory> page = followHistoryRepository.findFollowList(memberInfo.getId(), memberInfo.getViewAdult(), pageRequest);
        List<Long> ids = page.stream()
                .map(history -> history.getSeriesId())
                .collect(Collectors.toList());

        List<FollowSeriesInfo> series = seriesRepository.findAllById(ids).stream()
                .map(item -> new FollowSeriesInfo(item))
                .collect(Collectors.toList());

        model.addAttribute("totalCount", page.getTotalElements());
        model.addAttribute("followSeries", series);
        return "views/member/follow_list";
    }

    @GetMapping("/mypage")
    public String mypage(@CurrentMember LoginMember memberInfo, Model model){
        return "views/member/mypage";
    }



}
