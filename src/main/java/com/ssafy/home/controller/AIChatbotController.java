package com.ssafy.home.controller;

import com.ssafy.home.ai.dto.AIChatRequest;
import com.ssafy.home.ai.dto.SemanticSearchResponse;
import com.ssafy.home.service.AIChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Chatbot", description = "AI 기반 부동산 분석 챗봇 API")
public class AIChatbotController {

    private final AIChatbotService aiChatbotService;

    @Operation(summary = "채팅 질의", description = "자연어 질문을 받아 SQL을 생성/실행하여 분석 결과를 반환합니다.")
    @PostMapping("/chat")
    public ResponseEntity<SemanticSearchResponse> chat(@RequestBody AIChatRequest request) {
        log.info("AI Chat Request: {}", request.getMessage());
        SemanticSearchResponse response = aiChatbotService.generateResponse(request.getMessage());
        return ResponseEntity.ok(response);
    }
}
