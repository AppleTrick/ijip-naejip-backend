package com.ssafy.home.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    public String sendVerificationCode(String toEmail) {
        String code = createVerificationCode();
        
        // 실제 SMTP 설정이 있는 경우:
        // SimpleMailMessage message = new SimpleMailMessage();
        // message.setTo(toEmail);
        // message.setSubject("SSAFY Home 회원가입 인증 코드");
        // message.setText("인증 코드: " + code);
        // javaMailSender.send(message);

        // SMTP가 없는 개발 환경용:
        log.info("인증 코드 발송 대상 {}: {}", toEmail, code);
        
        return code;
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
