package com.ssafy.home.ai.controller;

import com.ssafy.home.ai.dto.AIChatRequest;
import com.ssafy.home.ai.dto.SemanticSearchResponse;
import com.ssafy.home.ai.service.AIChatbotService;
import com.ssafy.home.ai.service.AIReportService;
import com.ssafy.home.dto.CommonResponse;
import com.ssafy.home.dto.User;
import com.ssafy.home.mapper.UserMapper;
import com.ssafy.home.util.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "AI Chatbot", description = "AI 기반 부동산 분석 챗봇 API")
public class AIChatbotController {

    private final AIChatbotService aiChatbotService;
    private final AIReportService aiReportService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    @Operation(summary = "채팅 질의 (로그인 필수)", description = "자연어 질문을 받아 SQL을 생성/실행하여 분석 결과를 반환하고 자동으로 보고서로 저장합니다. 로그인한 사용자만 사용 가능합니다.")
    @PostMapping("/chat")
    public ResponseEntity<CommonResponse<SemanticSearchResponse>> chat(
            HttpServletRequest request,
            @RequestBody AIChatRequest chatRequest
    ) {
        log.info("AI Chat Request: {}", chatRequest.getMessage());

        // 로그인 확인 (필수)
        Long userId;
        try {
            userId = getUserIdFromToken(request);
        } catch (IllegalStateException e) {
            log.warn("로그인하지 않은 사용자의 채팅 시도: {}", e.getMessage());
            return ResponseEntity.status(401)
                    .body(CommonResponse.fail("401", "로그인이 필요합니다."));
        }

        // AI 분석 수행
        SemanticSearchResponse response = aiChatbotService.generateResponse(chatRequest.getMessage());

        // 보고서 자동 저장
        try {
            aiReportService.saveReportAutomatically(userId, chatRequest.getMessage(), response);
        } catch (Exception e) {
            log.error("보고서 자동 저장 실패 (계속 진행): {}", e.getMessage());
            // 저장 실패해도 응답은 정상 반환
        }

        return ResponseEntity.ok(CommonResponse.success(response));
    }

    private Long getUserIdFromToken(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        String email = jwtTokenProvider.getEmail(token);
        User user = userMapper.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));
        return user.getId();
    }
}
