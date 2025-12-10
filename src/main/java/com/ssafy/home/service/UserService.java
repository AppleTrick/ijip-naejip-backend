package com.ssafy.home.service;

import com.ssafy.home.dto.User;
import java.util.Map;

public interface UserService {
    void signup(User user);
    Map<String, String> login(String email, String password);
    void resetPassword(String email);
    void sendJoinCertificationMail(String email);
    boolean verifyEmail(String email, String code);
    void updateUser(User user);
    User getUser(String email);
    void changePassword(String email, String currentPassword, String newPassword);
}
