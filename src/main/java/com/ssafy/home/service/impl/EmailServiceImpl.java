package com.ssafy.home.service.impl;

import com.ssafy.home.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Override
    public String sendVerificationCode(String toEmail) {
        String code = createVerificationCode();
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("SSAFY Home 회원가입 인증 코드");
            message.setText("인증 코드: " + code);
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("메일 전송 실패: {}", e.getMessage(), e);
            throw new RuntimeException("메일 전송에 실패했습니다. 관리자에게 문의하세요.", e);
        }

        log.info("인증 코드 발송 성공: {}", toEmail);
        return code;
    }

    @Override
    public void sendTemporaryPassword(String toEmail, String tempPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("SSAFY Home 임시 비밀번호 발급");
            message.setText("임시 비밀번호: " + tempPassword + "\n로그인 후 반드시 비밀번호를 변경해주세요.");
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("메일 전송 실패: {}", e.getMessage(), e);
            throw new RuntimeException("메일 전송에 실패했습니다. 관리자에게 문의하세요.", e);
        }

        log.info("임시 비밀번호 발송 성공: {}", toEmail);
    }

    private String createVerificationCode() {
        Random random = new Random();
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            key.append(random.nextInt(10));
        }
        return key.toString();
    }
}
