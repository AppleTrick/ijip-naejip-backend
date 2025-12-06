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
}
