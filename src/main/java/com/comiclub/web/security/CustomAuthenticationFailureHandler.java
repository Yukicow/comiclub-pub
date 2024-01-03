package com.comiclub.web.security;

import com.comiclub.web.result.CommonResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final ObjectMapper mapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        exception.printStackTrace();

        CommonResult<String> result = new CommonResult<>(HttpServletResponse.SC_BAD_REQUEST, "FAIL", null);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().print(mapper.writeValueAsString(result));
        response.getWriter().flush();
    }
}
