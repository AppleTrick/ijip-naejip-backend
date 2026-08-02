package com.ssafy.home.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * refreshToken 쿠키 생성/만료 처리를 한 곳에서 관리.
 * UserController(로그인/로그아웃)와 OAuth2LoginSuccessHandler(소셜 로그인)가 각자
 * ResponseCookie를 직접 만들면서 secure/sameSite 값이 흩어져 있던 것을 통합.
 */
@Component
public class RefreshTokenCookieProvider {

    private static final String COOKIE_NAME = "refreshToken";
    private static final long MAX_AGE_SECONDS = 60 * 60 * 24 * 7; // 7일

    @Value("${app.cookie-secure:false}")
    private boolean secure;

    public ResponseCookie create(String refreshToken) {
        return ResponseCookie.from(COOKIE_NAME, refreshToken)
                .path("/")
                .sameSite("Lax") // Strict였으면 외부 링크(카카오톡 공유 등)로 처음 들어올 때 쿠키가 안 실려서 로그인 세션이 끊긴 것처럼 보임
                .httpOnly(true)
                .secure(secure) // application-prod.properties: app.cookie-secure=true
                .maxAge(MAX_AGE_SECONDS)
                .build();
    }

    public ResponseCookie clear() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .path("/")
                .sameSite("Lax")
                .httpOnly(true)
                .secure(secure)
                .maxAge(0)
                .build();
    }
}
