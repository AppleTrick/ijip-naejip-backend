package com.ssafy.home.ai.service.impl;

import com.ssafy.home.ai.dto.RegionInfo;
import com.ssafy.home.ai.dto.SemanticSearchResponse;
import com.ssafy.home.ai.exception.AIUnavailableException;
import com.ssafy.home.ai.service.AIChatbotService;
import com.ssafy.home.ai.service.DatabaseQueryTool;
import com.ssafy.home.ai.service.PromptTemplateManager;
import com.ssafy.home.ai.service.QueryResultCollector;
import com.ssafy.home.ai.service.RegionValidator;
import com.ssafy.home.dto.AddressResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Tool Calling 기반 부동산 분석 어시스턴트
 * AI가 DatabaseQueryTool을 사용하여 자율적으로 SQL을 실행하고 분석합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatbotServiceImpl implements AIChatbotService {

    private final ChatClient.Builder chatClientBuilder;
    private final PromptTemplateManager promptTemplateManager;
    private final QueryResultCollector queryResultCollector;
    private final RegionValidator regionValidator;

    /** 기본 모델이 분당 토큰 한도에 걸리면 재시도할 모델 (Groq 한도는 모델별로 따로 적용된다) */
    @Value("${ai.chat.fallback-model:openai/gpt-oss-20b}")
    private String fallbackModel;

    /**
     * 사용자 메시지에 대한 응답 생성
     * AI가 Tool을 사용하여 자율적으로 데이터를 조회하고 분석합니다.
     *
     * @param userMessage 사용자 질문
     * @return 분석 결과
     */
    @Override
    public SemanticSearchResponse generateResponse(String userMessage) {
        try {
            // 이전 요청의 결과 초기화
            queryResultCollector.clear();

            // 1. 사용자 메시지에서 지역명 추출 및 검증
            log.info("Validating regions in user message: {}", userMessage);
            List<RegionInfo> validatedRegions = regionValidator.extractAndValidateRegions(userMessage);

            // 2. System Prompt 준비 + 검증된 지역 정보 추가
            String systemPrompt = promptTemplateManager.getSystemPrompt();

            // 검증된 지역이 있으면 프롬프트에 추가
            if (!validatedRegions.isEmpty()) {
                String regionContext = regionValidator.buildRegionContext(validatedRegions);
                systemPrompt = systemPrompt + regionContext;
                log.info("Added {} validated regions to system prompt", validatedRegions.size());
            } else {
                log.info("No regions detected in user message");
            }

            log.info("Processing query with Tool Calling: {}", userMessage);

            String response;
            try {
                response = ask(systemPrompt, userMessage, null);
            } catch (RuntimeException e) {
                if (!isRateLimited(e)) {
                    throw e;
                }
                log.warn("기본 모델 한도 초과 — {}로 재시도", fallbackModel);
                queryResultCollector.clear();
                response = ask(systemPrompt, userMessage, fallbackModel);
            }

            log.info("AI Response generated successfully");

            // Tool 실행 중 수집된 샘플 아파트 가져오기
            List<AddressResponse> sampleApartments = queryResultCollector.getSampleApartments();
            log.info("Extracted {} sample apartments from tool results", sampleApartments.size());

            // 응답을 SemanticSearchResponse로 변환
            return SemanticSearchResponse.builder()
                    .analysis(response)
                    .results(sampleApartments)  // 샘플 아파트를 results에 포함
                    .build();

        } catch (Exception e) {
            log.error("Error processing chat request", e);
            throw new AIUnavailableException("AI 응답 생성 실패", isRateLimited(e), e);
        } finally {
            // 반드시 정리
            queryResultCollector.clear();
        }
    }

    private String ask(String systemPrompt, String userMessage, String model) {
        ChatClient.ChatClientRequestSpec request = chatClientBuilder.build().prompt()
                .system(systemPrompt)
                .user(userMessage)
                .functions("executeDatabaseQuery");
        if (model != null) {
            request = request.options(OpenAiChatOptions.builder().model(model).build());
        }
        return request.call().content();
    }

    private boolean isRateLimited(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            String message = t.getMessage();
            if (message != null && (message.contains("429") || message.contains("rate_limit_exceeded"))) {
                return true;
            }
        }
        return false;
    }
}
