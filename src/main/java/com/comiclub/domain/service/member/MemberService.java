package com.comiclub.domain.service.member;

import com.comiclub.domain.entity.member.Member;
import com.comiclub.web.exception.AlreadyExistException;
import com.comiclub.domain.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {


    private final MemberRepository memberRepository;


    public Long join(Member member) {
        validateDuplicatedMember(member);
        Member savedMember = memberRepository.save(member);
        return savedMember.getId();
    }

    private void validateDuplicatedMember(Member member) {
        Member findMember = memberRepository.findByLoginId(member.getLoginId())
                .orElse(null);

        if (findMember != null)
            throw new AlreadyExistException("The login info is already exist");
    }


    public void changeViewAdult(Long id, boolean viewAdult) {
        memberRepository.changeViewAdult(id, viewAdult);
    }


}
