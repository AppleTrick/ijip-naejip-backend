package com.ssafy.home.config;

import com.ssafy.home.filter.JwtAuthenticationFilter;
import com.ssafy.home.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final com.ssafy.home.service.CustomOAuth2UserService customOAuth2UserService;
    private final com.ssafy.home.oauth.handler.OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    private final com.ssafy.home.filter.JwtExceptionFilter jwtExceptionFilter;
    private final com.ssafy.home.filter.JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final com.ssafy.home.filter.JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(
                    "/", "/index.html", "/*.html", "/css/**", "/js/**", "/img/**", "/favicon.ico",
                    "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/swagger/**", "/swagger", "/swagger-resources/**", "/api-docs/**", "/api-docs", "/webjars/**",
                    "/api/v1/swagger-ui/**", "/api/v1/v3/api-docs/**", "/api/v1/swagger-ui.html", "/api/v1/swagger/**", "/api/v1/swagger", "/api/v1/swagger-resources/**", "/api/v1/api-docs/**", "/api/v1/api-docs", "/api/v1/webjars/**"
                );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/error",
                    "/user/signup", "/user/login", "/user/check-email", "/user/email-verification/**",
                    "/user/reset-password",
                    "/oauth2/**", "/login/oauth2/code/**",
                    "/area", "/area/**", "/dongcode/**", "/apartments/**", "/ai/**", "/favorites/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(oAuth2LoginSuccessHandler)
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtExceptionFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:5174", "http://localhost:8080", "http://100.87.161.18:81", "https://ijip.changhee.dev")); // Vue 개발 서버 & Spring Boot & 배포
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
