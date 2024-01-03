package com.comiclub.web.security;


import com.comiclub.web.security.jwt.JwtAuthSuccessHandler;
import com.comiclub.web.security.jwt.JwtAuthorizationFilter;
import com.comiclub.web.security.jwt.JwtLogoutSuccessHandler;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthSuccessHandler jwtAuthSuccessHandler;
    private final JwtLogoutSuccessHandler jwtLogoutSuccessHandler;
    private final CustomAuthenticationFailureHandler failureHandler;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource()).and()
                .httpBasic().disable()
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(new JwtAuthorizationFilter(), BasicAuthenticationFilter.class)
                .authorizeHttpRequests(request -> request
                        .dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
                        .requestMatchers("/", "/error/**", "/club/**", "/clubs/**", "/fwBoards/**", "/series/**", "/member/view-adult").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/mywork/**", "/member/**").hasRole("MEMBER")
                        .requestMatchers("/login", "/join").anonymous()
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login    // form 방식 로그인 사용
                        .loginPage("/login")    // 커스텀 로그인 페이지 지정 => Default : GET /login
                        .loginProcessingUrl("/login")    // 로그인 정볼를 submit 받을 url => Default : POST /login
                        .usernameParameter("loginId")    // Form에서 아이디에 해당하는 input의 name태그 값
                        .passwordParameter("password")    // Form에서 비밀번호에 해당하는 input의 name태그 값
                        .permitAll()
                        .successHandler(jwtAuthSuccessHandler) // 성공 시 커스텀
                        .failureHandler(failureHandler) // 실패 시 커스텀
                )
                .logout(logout -> logout
                        .logoutSuccessHandler(jwtLogoutSuccessHandler)
                        .deleteCookies("accessToken")
                        .permitAll()
                )
                .getOrBuild();

    }


    // CORS 허용 적용
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.addAllowedOrigin("https://comiclub.co.kr,https://www.comiclub.co.kr");
        configuration.setAllowedMethods(Arrays.asList("GET","POST", "PATCH", "OPTIONS", "PUT","DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        return web -> {
            web.ignoring()
                    .requestMatchers("/img/**", "/js/**", "/css/**", "/font/**", "/file/**");
        };
    }


}
