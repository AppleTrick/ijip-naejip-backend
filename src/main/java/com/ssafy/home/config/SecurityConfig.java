package com.ssafy.home.config;

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
            .csrf(csrf -> csrf.disable()) // REST API이므로 CSRF 비활성화
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll()        // API 전체 허용
                .requestMatchers("/admin.html").permitAll()    // 관리자 페이지 허용
                .requestMatchers("/actuator/**").permitAll()   // Health check 허용
                .anyRequest().permitAll()                       // 나머지도 일단 허용
            );

        return http.build();
    }
}

