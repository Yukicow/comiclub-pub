package com.comiclub.web.security.jwt;

import com.comiclub.web.util.bean.BeanUtils;
import com.comiclub.domain.repository.member.MemberRepository;
import com.comiclub.web.exception.NotFoundException;
import com.comiclub.web.security.Account;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    private final MemberRepository memberRepository;

    public JwtAuthorizationFilter() {
        this.tokenProvider = (TokenProvider) BeanUtils.getBean("tokenProvider");
        this.memberRepository = (MemberRepository) BeanUtils.getBean("memberRepository");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String accessToken = getAccessToken(request);

        Account user = parseUserSpecification(accessToken);
        if(user != null){
            AbstractAuthenticationToken authenticated = UsernamePasswordAuthenticationToken.authenticated(user, accessToken, user.getAuthorities());
            authenticated.setDetails(new WebAuthenticationDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticated);
        }

        filterChain.doFilter(request, response);
    }

    private String getAccessToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if(cookies == null) return null;

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("accessToken"))
                .findAny()
                .map(cookie -> cookie.getValue())
                .orElse(null);
    }


    private Account parseUserSpecification(String token) {
        Long memberId = Optional.ofNullable(token)
                .filter(subject -> subject.length() >= 10)
                .map(it -> Long.parseLong(tokenProvider.validateTokenAndGetSubject(it)))
                .orElse(null);

        return memberId != null
                ? new Account(
                        memberRepository.findById(memberId)
                                .orElseThrow(() -> new NotFoundException("Invalid AccessToken '" + memberId + "'"))
                    )
                : null;
    }
}