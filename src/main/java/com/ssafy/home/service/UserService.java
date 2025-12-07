package com.ssafy.home.service;

import com.ssafy.home.dto.User;
import com.ssafy.home.mapper.UserMapper;
import com.ssafy.home.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    // 인메모리 인증 코드 저장소 (Email -> Code)
    private final java.util.Map<String, String> verificationCodes = new java.util.concurrent.ConcurrentHashMap<>();

    @Transactional
    public void signup(User user) {
        if (userMapper.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.save(user);
    }

    public Map<String, String> login(String email, String password) {
        User user = userMapper.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtTokenProvider.createToken(user.getEmail(), user.getRole().name());
        return Map.of("token", token, "name", user.getName());
    }

    @Transactional
    public void resetPassword(String email) {
        User user = userMapper.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        if (user.getSocialType() != User.SocialType.NONE) {
            throw new IllegalArgumentException("소셜 로그인 사용자는 비밀번호를 초기화할 수 없습니다.");
        }

        // 8자리 임시 비밀번호 생성 (영문+숫자)
        String tempPassword = java.util.UUID.randomUUID().toString().substring(0, 8);
        
        // 비밀번호 암호화 및 업데이트
        user.setPassword(passwordEncoder.encode(tempPassword));
        userMapper.update(user);

        // 이메일 발송
        emailService.sendTemporaryPassword(email, tempPassword);
    }

    public void sendJoinCertificationMail(String email) {
        if (userMapper.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        String code = emailService.sendVerificationCode(email);
        verificationCodes.put(email, code);
    }

    public boolean verifyEmail(String email, String code) {
        String storedCode = verificationCodes.get(email);
        if (storedCode != null && storedCode.equals(code)) {
            verificationCodes.remove(email); // 인증 성공 시 코드 삭제
            return true;
        }
        return false;
    }

    @Transactional
    public void updateUser(User user) {
        User existingUser = userMapper.findByEmail(user.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 변경 가능한 필드만 업데이트 (null이 아닌 경우에만)
        if (user.getAgeGroup() != null) existingUser.setAgeGroup(user.getAgeGroup());
        if (user.getJob() != null) existingUser.setJob(user.getJob());
        if (user.getGender() != null) existingUser.setGender(user.getGender());
        if (user.getMaritalStatus() != null) existingUser.setMaritalStatus(user.getMaritalStatus());
        
        userMapper.update(existingUser);
    }

    public User getUser(String email) {
        return userMapper.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}
