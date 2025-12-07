package com.ssafy.home.service;

import com.ssafy.home.dto.User;
import com.ssafy.home.mapper.UserMapper;
import com.ssafy.home.oauth.info.OAuth2UserInfo;
import com.ssafy.home.oauth.info.impl.GoogleOAuth2UserInfo;
import com.ssafy.home.oauth.info.impl.KakaoOAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserMapper userMapper;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("OAuth2 User Attributes: {}", oAuth2User.getAttributes());

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = null;

        if (registrationId.equals("kakao")) {
            oAuth2UserInfo = new KakaoOAuth2UserInfo(oAuth2User.getAttributes());
        } else if (registrationId.equals("google")) {
            oAuth2UserInfo = new GoogleOAuth2UserInfo(oAuth2User.getAttributes());
        } else {
            log.error("Unsupported OAuth2 Provider: {}", registrationId);
            throw new OAuth2AuthenticationException("Unsupported OAuth2 Provider: " + registrationId);
        }

        String provider = oAuth2UserInfo.getProvider();
        String providerId = oAuth2UserInfo.getProviderId();
        String email = oAuth2UserInfo.getEmail();
        String name = oAuth2UserInfo.getName();
        String profileImage = oAuth2UserInfo.getProfileImage();

        User user = null;
        
        // 1. 이메일로 조회
        if (email != null && !email.isEmpty()) {
            user = userMapper.findByEmail(email).orElse(null);
        }

        // 2. 이메일로 못 찾았거나 이메일이 없는 경우, SocialID로 조회
        if (user == null) {
            java.util.Map<String, Object> params = java.util.Map.of(
                "socialType", com.ssafy.home.dto.User.SocialType.valueOf(provider.toUpperCase()),
                "socialId", providerId
            );
            user = userMapper.findBySocialId(params).orElse(null);
        }

        if (user != null) {
            // 기존 가입자라면 정보 업데이트
            user.setName(name);
            user.setProfileImage(profileImage);
            user.setSocialType(User.SocialType.valueOf(provider.toUpperCase()));
            user.setSocialId(providerId);
            userMapper.update(user);
        } else {
            // 신규 가입자
            // 신규 가입자
            if (email == null || email.isEmpty()) {
                // 이메일이 없는 경우 (카카오 등): 임의의 이메일 생성하여 자동 가입
                email = provider + "_" + providerId + "@social.user";
            }
            
            user = User.builder()
                    .email(email)
                    .name(name)
                    .profileImage(profileImage)
                    .role(User.Role.ROLE_USER)
                    .socialType(User.SocialType.valueOf(provider.toUpperCase()))
                    .socialId(providerId)
                    .isEmailVerified(true) // 소셜 로그인은 이메일 인증된 것으로 간주
                    .build();
            userMapper.save(user);
        }

        return oAuth2User;
    }
}
