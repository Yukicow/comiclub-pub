package com.comiclub.web.security.jwt;

import com.comiclub.web.result.CommonResult;
import com.comiclub.web.security.Account;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;



@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthSuccessHandler implements AuthenticationSuccessHandler {
    private final RequestCache requestCache = new HttpSessionRequestCache();
    private final ObjectMapper mapper;
    private final TokenProvider tokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        boolean accessTokenExist = false;
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("accessToken"))
                    accessTokenExist = true;
            }
            if(accessTokenExist)
                throw new IllegalStateException("이미 로그인 상태입니다.");
        }


        Account member = (Account) authentication.getPrincipal();

        String accessToken = tokenProvider.createAccessToken(member.getId());
        ResponseCookie newAccessTokenCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(true)
                .maxAge(6 * 60 * 60)
                .path("/")
                .sameSite("Strict")
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, newAccessTokenCookie.toString());

        clearSession(request);

        SavedRequest savedRequest = requestCache.getRequest(request, response);

        /**
         * prevPage가 존재하는 경우 = 사용자가 직접 /auth/login 경로로 로그인 요청
         * 기존 Session의 prevPage attribute 제거
         */
        String prevPage = (String) request.getSession().getAttribute("prevPage");
        if (prevPage != null) {
            request.getSession().removeAttribute("prevPage");
        }

        // 기본 URI
        String uri = "/";

        /**
         * savedRequest 존재하는 경우 = 인증 권한이 없는 페이지 접근
         * Security Filter가 인터셉트하여 savedRequest에 세션 저장
         */
        if (savedRequest != null) {
            uri = savedRequest.getRedirectUrl();
        } else if (prevPage != null && !prevPage.equals("")) {
            // 회원가입 - 로그인으로 넘어온 경우 "/"로 redirect
            if (prevPage.contains("/join")) {
                uri = "/";
            } else {
                uri = prevPage;
            }
        }

        CommonResult<String> result = new CommonResult<>(HttpServletResponse.SC_OK, "REDIRECT", uri);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().print(mapper.writeValueAsString(result));
        response.getWriter().flush();
    }

    // 로그인 성공 시 실패 후 남아있던 에러 세션 제거
    protected void clearSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }
}
