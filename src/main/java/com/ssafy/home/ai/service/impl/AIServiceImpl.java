package com.ssafy.home.ai.service.impl;

import com.ssafy.home.ai.dto.*;
import com.ssafy.home.ai.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final ChatModel chatModel;
    private final com.ssafy.home.mapper.ApartmentMapper apartmentMapper;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    private String callGPT(String promptText, String systemMessageText) {
        // 입력값 트림 처리
        String trimmedPrompt = promptText != null ? promptText.trim() : "";
        String trimmedSystem = systemMessageText != null ? systemMessageText.trim() : "";
        
        log.info("AI Request - System: [{}], User: [{}]", trimmedSystem, trimmedPrompt);

        try {
            // 일부 프록시에서 system role 처리가 미흡할 수 있으므로, 
            // 하나의 UserMessage 안에 시스템 지시사항을 포함하여 전달하는 방식(Simple Prompt)으로 시도해봅니다.
            String combinedPrompt = String.format("지시사항: %s\n질문: %s", trimmedSystem, trimmedPrompt);
            UserMessage userMessage = new UserMessage(combinedPrompt);
            
            Prompt prompt = new Prompt(List.of(userMessage));
            log.debug("Sending Simple Prompt to Spring AI");
            
            org.springframework.ai.chat.model.ChatResponse response = chatModel.call(prompt);
            if (response == null || response.getResult() == null) {
                log.warn("AI Response was null");
                return "AI로부터 응답을 받지 못했습니다.";
            }
            String content = response.getResult().getOutput().getContent();
            
            log.info("AI Response received successfully");
            return content;
        } catch (Exception e) {
            log.error("Error calling Spring AI (400/403 Debug): {}", e.getMessage(), e);
            return "AI 분석 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    @Override
    public SemanticSearchResponse performSemanticSearch(String query) {
        String systemMsg = "당신은 부동산 전문가입니다. 사용자의 검색 의도를 분석하여 답변하고, " +
                "검색에 도움이 될 핵심 키워드(지역명, 아파트명 등)를 쉼표로 구분하여 답변 마지막에 '[KEYWORDS: 키워드1, 키워드2]' 형식으로 포함해주세요. 한국어로 답변하세요.";
        
        String aiResponse = callGPT(query, systemMsg);
        
        List<com.ssafy.home.dto.mapper.ApartmentBasicInfo> searchResults = new ArrayList<>();
        String analysis = aiResponse;
        
        // 키워드 추출 및 실제 DB 검색
        if (aiResponse.contains("[KEYWORDS:")) {
            try {
                int start = aiResponse.lastIndexOf("[KEYWORDS:") + 10;
                int end = aiResponse.lastIndexOf("]");
                String keywordsStr = aiResponse.substring(start, end);
                List<String> keywords = List.of(keywordsStr.split(","))
                        .stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
                
                if (!keywords.isEmpty()) {
                    searchResults = apartmentMapper.searchApartmentsByKeywords(keywords);
                }
                // 분석 텍스트에서 키워드 부분 제거
                analysis = aiResponse.substring(0, aiResponse.lastIndexOf("[KEYWORDS:")).trim();
            } catch (Exception e) {
                log.error("Failed to extract keywords or search DB: {}", e.getMessage());
            }
        }

        return SemanticSearchResponse.builder()
                .results(searchResults.stream()
                        .<com.ssafy.home.dto.AddressResponse>map(info -> com.ssafy.home.dto.AddressResponse.builder()
                                .aptSeq(info.aptSeq())
                                .aptName(info.aptName())
                                .dongName(info.address()) // Using address as dongName for display
                                .build())
                        .collect(java.util.stream.Collectors.toList()))
                .analysis(analysis)
                .build();
    }

    @Override
    public ParseFilterResponse parseFilter(String query) {
        String prompt = "다음 자연어 쿼리를 분석하여 JSON 형식의 필터 조건으로 변환해주세요: '" + query + "'. " +
                "JSON 형식 예시: {\"priceRange\": {\"min\": 0, \"max\": 15}, \"areaRange\": {\"min\": 20, \"max\": 40}}. " +
                "단위는 억(가격), 평(면적)입니다. 오직 JSON만 반환하세요. JSON 외의 설명은 금지합니다.";
        
        String analysis = callGPT(prompt, "당신은 자연어를 JSON 필터 조건으로 변환하는 전문가입니다.");
        
        FilterConditions filters;
        try {
            String jsonPart = analysis.substring(analysis.indexOf("{"), analysis.lastIndexOf("}") + 1);
            filters = objectMapper.readValue(jsonPart, FilterConditions.class);
        } catch (Exception e) {
            log.error("Failed to parse AI response to FilterConditions: {}", e.getMessage());
            filters = FilterConditions.builder()
                    .priceRange(FilterConditions.PriceRange.builder().min(0).max(100).build())
                    .areaRange(FilterConditions.AreaRange.builder().min(0).max(200).build())
                    .build();
        }

        return ParseFilterResponse.builder()
                .filters(filters)
                .analysis(analysis)
                .build();
    }

    @Override
    public FraudAnalysisResponse performFraudAnalysis(FraudAnalysisRequest request) {
        String prompt = String.format("주소: %s, 매매가: %d만, 보증금: %d만, 선순위채권: %d만. 이 매물의 전세사기 위험도를 분석해주세요.",
                request.getAddress(), request.getMarketValue(), request.getDeposit(), request.getPriorDebt());
        
        String analysis = callGPT(prompt, "당신은 전세사기 방지 전문가입니다. 입력된 데이터를 바탕으로 위험도를 진단해주세요. 한국어로 답변하세요.");
        
        int debtRatio = 0;
        if (request.getMarketValue() > 0) {
            debtRatio = (int) (((request.getDeposit() + request.getPriorDebt()) * 100) / request.getMarketValue());
        }

        String grade = debtRatio >= 80 ? "DANGER" : (debtRatio >= 70 ? "WARNING" : "SAFE");

        return FraudAnalysisResponse.builder()
                .safetyGrade(grade)
                .message(analysis)
                .debtRatio(debtRatio)
                .build();
    }

    @Override
    public String getRegionalAnalysis(String areaCode, String apartmentName) {
        String prompt = String.format("%s 지역의 %s 아파트에 대한 상세 분석을 제공해주세요.", areaCode, apartmentName);
        return callGPT(prompt, "당신은 지역 기반 부동산 에이전트입니다. 한국어로 답변하세요.");
    }

    @Override
    public String getComparisonSummary(String comparisonData) {
        String prompt = "다음 매물 데이터를 비교하여 요약해주세요: " + comparisonData;
        return callGPT(prompt, "당신은 부동산 비교 전문가입니다. 각 매물의 장단점을 요약하여 한국어로 답변하세요.");
    }
}
