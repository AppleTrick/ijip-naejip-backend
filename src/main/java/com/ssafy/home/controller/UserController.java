package com.ssafy.home.controller;

import com.ssafy.home.dto.User;
import com.ssafy.home.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "User API (회원 관리)", description = "사용자 인증 및 회원 관리 API")
public class UserController {

    private final UserService userService;
    private final com.ssafy.home.util.JwtTokenProvider jwtTokenProvider;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    public ResponseEntity<?> signup(@RequestBody User user) {
        System.out.println("Signup request received: " + user);
        try {
            userService.signup(user);
            return ResponseEntity.ok("회원가입 성공");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest, jakarta.servlet.http.HttpServletResponse response) {
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");
            Map<String, String> result = userService.login(email, password);
            
            String refreshToken = result.get("refreshToken");
            String accessToken = result.get("accessToken");
            String name = result.get("name");

            // Refresh Token을 HttpOnly 쿠키로 설정
            org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from("refreshToken", refreshToken)
                    .path("/")
                    .sameSite("Strict")
                    .httpOnly(true)
                    .secure(false) // 로컬 개발 환경에서는 false, 배포 시 true 권장
                    .maxAge(60 * 60 * 24 * 7) // 7일
                    .build();
            response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString());

            return ResponseEntity.ok(Map.of("accessToken", accessToken, "name", name));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reissue")
    @Operation(summary = "토큰 재발급", description = "Refresh Token을 이용하여 새로운 Access Token을 발급받습니다.")
    public ResponseEntity<?> reissue(@CookieValue(value = "refreshToken", required = false) @Parameter(description = "리프레시 토큰 (쿠키)", required = false) String refreshToken) {
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body("Invalid or missing Refresh Token");
        }

        try {
            String email = jwtTokenProvider.getEmail(refreshToken);
            // 실제로는 DB에서 User 정보를 조회하여 Role 등을 가져와야 함.
            // 여기서는 간단히 User 정보를 조회
            User user = userService.getUser(email);
            String newAccessToken = jwtTokenProvider.createAccessToken(email, user.getRole().name());

            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body("Failed to reissue token");
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "사용자를 로그아웃 처리합니다. 클라이언트 측에서 Access Token을 삭제해야 합니다.")
    public ResponseEntity<?> logout() {
        // JWT 방식은 서버 세션이 없으므로 클라이언트에게 성공 응답만 보냄
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "비밀번호 초기화 요청", description = "이메일로 임시 비밀번호를 전송합니다.")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            userService.resetPassword(email);
            return ResponseEntity.ok("임시 비밀번호가 이메일로 전송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/email-verification/request")
    @Operation(summary = "이메일 인증 코드 요청", description = "회원가입을 위한 이메일 인증 코드를 전송합니다.")
    public ResponseEntity<?> requestEmailVerification(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            userService.sendJoinCertificationMail(email);
            return ResponseEntity.ok("인증 코드가 전송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/email-verification/confirm")
    @Operation(summary = "이메일 인증 코드 확인", description = "전송된 이메일 인증 코드를 검증합니다.")
    public ResponseEntity<?> confirmEmailVerification(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String code = request.get("code");
            if (userService.verifyEmail(email, code)) {
                return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
            } else {
                return ResponseEntity.badRequest().body("인증 코드가 올바르지 않습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PutMapping("/update")
    @Operation(summary = "회원정보 수정", description = "로그인한 사용자의 정보를 수정합니다.")
    public ResponseEntity<?> updateProfile(@RequestBody User user, org.springframework.security.core.Authentication authentication) {
        try {
            // JWT 토큰에서 사용자 이메일 추출
            String email = authentication.getName();
            user.setEmail(email); // 토큰의 이메일로 강제 설정 (보안)
            
            userService.updateUser(user);
            
            // 업데이트된 사용자 정보 반환
            User updatedUser = userService.getUser(email);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/info")
    @Operation(summary = "회원정보 조회", description = "로그인한 사용자의 상세 정보를 조회합니다.")
    public ResponseEntity<?> getUserInfo(org.springframework.security.core.Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userService.getUser(email);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/change-password")
    @Operation(summary = "비밀번호 변경", description = "로그인한 사용자의 비밀번호를 변경합니다.")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request, org.springframework.security.core.Authentication authentication) {
        try {
            String email = authentication.getName();
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            
            userService.changePassword(email, currentPassword, newPassword);
            return ResponseEntity.ok("비밀번호가 변경되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
