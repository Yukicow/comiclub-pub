package com.comiclub.web.security;

import com.comiclub.domain.entity.member.Member;
import com.comiclub.domain.entity.member.Role;
import com.comiclub.domain.repository.member.MemberRepository;
import com.comiclub.web.contoller.common.form.JoinForm;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {
    private final MemberRepository memberRepository;
    private final PasswordEncoder encoder;    // 추가

    @Override
    public void run(ApplicationArguments args) {
        JoinForm joinForm = new JoinForm();
        joinForm.setLoginId("admin");
        joinForm.setPassword("admin");
        joinForm.setNickname("Admin");

        memberRepository.save(Member.createNewMember(
                joinForm,
                encoder,
                Role.ADMIN
        ));
    }
}