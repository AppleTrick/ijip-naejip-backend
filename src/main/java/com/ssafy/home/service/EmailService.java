package com.ssafy.home.service;

public interface EmailService {
    String sendVerificationCode(String toEmail);
    void sendTemporaryPassword(String toEmail, String tempPassword);
}
