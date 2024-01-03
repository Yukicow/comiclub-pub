package com.comiclub.web.security;

import com.comiclub.domain.entity.member.Member;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Slf4j @Getter
public class Account extends User {

    private LoginMember member;

    public Account(Member member) {
        super(member.getLoginId(), member.getPassword(), List.of(new SimpleGrantedAuthority(member.getRole().getRole())));
        LoginMember loginMember = new LoginMember(member);
        this.member = loginMember;
    }


    public Long getId() {
        return member.getId();
    }
    public String getNickname() {
        return member.getNickname();
    }

    public String getLoginId() {
        return member.getLoginId();
    }

    public Boolean getAdult() {
        return member != null ? member.getAdult() : false;
    }

    public Boolean getViewAdult() {
        return member != null ? member.getViewAdult() : false;
    }

    public Boolean isAuthenticated(){
        return member != null ? true : false;
    }

    public boolean hasRole(String role){
        return this.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_" + role));
    }


}
