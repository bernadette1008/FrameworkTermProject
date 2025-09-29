package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        // 로그인, 회원가입, 정적 리소스는 누구나 접근 가능
                        .requestMatchers("/", "/login", "/register-**", "/logout",
                                "/css/**", "/js/**", "/images/**", "/static/**").permitAll()
                        // 나머지는 모든 접근 허용 (자체 인증 시스템 사용)
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .disable()  // Spring Security 기본 로그인 폼 비활성화
                )
                .logout(logout -> logout
                        .disable()  // Spring Security 기본 로그아웃 비활성화
                )
                .httpBasic(basic -> basic
                        .disable()  // HTTP Basic 인증 비활성화
                )
                .csrf(csrf -> csrf
                        .disable()  // CSRF 보호 비활성화
                );

        return http.build();
    }
}