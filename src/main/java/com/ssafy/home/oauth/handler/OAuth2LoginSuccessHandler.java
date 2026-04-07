package com.ssafy.home.oauth.handler;

import com.ssafy.home.dto.User;
import com.ssafy.home.mapper.UserMapper;
import com.ssafy.home.oauth.info.OAuth2UserInfo;
import com.ssafy.home.oauth.info.impl.GoogleOAuth2UserInfo;
import com.ssafy.home.oauth.info.impl.KakaoOAuth2UserInfo;
import com.ssafy.home.util.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = authToken.getAuthorizedClientRegistrationId();
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        OAuth2UserInfo oAuth2UserInfo = null;
        if ("kakao".equals(registrationId)) {
            oAuth2UserInfo = new KakaoOAuth2UserInfo(oAuth2User.getAttributes());
        } else if ("google".equals(registrationId)) {
            oAuth2UserInfo = new GoogleOAuth2UserInfo(oAuth2User.getAttributes());
        }

        String email = oAuth2UserInfo.getEmail();
        User user = null;

        if (email == null || email.isEmpty()) {
            // 이메일이 없는 경우 (카카오 등): socialId로 사용자 조회 시도
            Map<String, Object> params = Map.of(
                "socialType", oAuth2User.getAttributes().get("socialType") != null ? oAuth2User.getAttributes().get("socialType") : (registrationId.equals("kakao") ? User.SocialType.KAKAO : User.SocialType.GOOGLE),
                "socialId", oAuth2UserInfo.getId()
            );
            user = userMapper.findBySocialId(params).orElse(null);

            if (user == null) {
                // 신규 가입 필요: 이메일 입력 페이지로 리다이렉트
                String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth/callback")
                        .queryParam("needsEmail", "true")
                        .queryParam("socialId", oAuth2UserInfo.getId())
                        .queryParam("socialType", registrationId.toUpperCase())
                        .build().toUriString();
                getRedirectStrategy().sendRedirect(request, response, targetUrl);
                return;
            }
        } else {
            // 이메일이 있는 경우
            user = userMapper.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());
        
        // 프론트엔드로 리다이렉트 (토큰 전달 - 쿠키 방식)
        // 로컬 개발 환경 가정: http://localhost:5173/oauth/callback
        
        // 쿠키 생성 (ResponseCookie 사용)
        org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from("refreshToken", refreshToken)
                .path("/")
                .sameSite("Strict")
                .httpOnly(true)
                .secure(false)
                .maxAge(60 * 60 * 24 * 7)
                .build();
        
        response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString());

        UriComponentsBuilder targetUrlBuilder = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth/callback")
                .queryParam("accessToken", accessToken);

        if (user.getAgeGroup() == null) {
            targetUrlBuilder.queryParam("needsAdditionalInfo", "true");
        }

        String targetUrl = targetUrlBuilder.build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
