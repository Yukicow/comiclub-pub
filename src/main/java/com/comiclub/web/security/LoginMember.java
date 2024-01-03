package com.comiclub.web.security;


import com.comiclub.domain.entity.member.Member;
import com.comiclub.domain.entity.member.Role;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginMember {

    private Long id;
    private String nickname;
    private String loginId;
    private Boolean adult;
    private Boolean viewAdult;
    private Role role;


    public LoginMember() {
    }

    public LoginMember(Member member) {
        this.id = member.getId();
        this.nickname = member.getNickname();
        this.loginId = member.getLoginId();
        this.adult = member.getAdult();
        this.viewAdult = member.getViewAdult();
        this.role = member.getRole();
    }
}
