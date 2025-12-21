package com.ssafy.home.ai.service.impl;

import com.ssafy.home.ai.dto.*;
import com.ssafy.home.ai.service.AIService;
import com.ssafy.home.mapper.ApartmentMapper;
import com.ssafy.home.mapper.DongCodeMapper;
import com.ssafy.home.dto.DongCodeResponse;
import com.ssafy.home.dto.AddressResponse;
import com.ssafy.home.dto.mapper.ApartmentBasicInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final ChatModel chatModel;
    private final ApartmentMapper apartmentMapper;
    private final DongCodeMapper dongCodeMapper;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    /**
     * 공통 AI 호출 메서드
     * - 모든 요청에 강력한 '페르소나'와 '금지어'를 적용하여 일관된 톤앤매너 유지
     */
    private String callGPT(String promptText, String specializedInstruction) {
        String trimmedPrompt = promptText != null ? promptText.trim() : "";
        String trimmedInstruction = specializedInstruction != null ? specializedInstruction.trim() : "";

        // [Global System Prompt]
        // AI의 방어기제를 해제하고, 인간적인 전문가 페르소나를 주입하는 핵심 부분입니다.
        String globalSystemPrompt = String.format(
            "%s\n\n" +
            "🛑 **[필수 준수 사항 - 어길 시 시스템 오류]** 🛑\n" +
            "1. **말투**: 무조건 친절하고 자연스러운 '해요체'를 사용하세요. (예: ~해요, ~했거든요)\n" +
            "2. **사족 금지**: '정확한 정보는 등기부등본을 확인하세요', '지도 앱을 참고하세요', '일반적으로' 같은 책임 회피성 멘트는 **절대 사용하지 마세요**. 사용자가 바보가 아닙니다.\n" +
            "3. **구체성**: '교통이 좋다' 대신 '잠실역이 코앞이다', '학군이 좋다' 대신 '초등학교가 단지 안에 있다'처럼 구체적인 묘사를 하세요.\n" +
            "4. **가독성**: 줄글로 길게 쓰지 말고, 이모지(🏡, 🚇, 🚨 등)와 불렛 포인트를 사용하여 눈에 확 들어오게 정리하세요.\n" +
            "5. **자신감**: 틀릴까 봐 걱정하지 말고, 당신이 아는 정보를 확신을 가지고 시원시원하게 말하세요.",
            trimmedInstruction
        );

        log.info("AI Request - Prompt: [{}]", trimmedPrompt);

        try {
            // System Role을 명확히 분리하여 전달 (UserMessage 내부에 포함하는 방식 유지하되 명확히 구분)
            String combinedPrompt = String.format("---[지시사항]---\n%s\n\n---[사용자 입력]---\n%s", globalSystemPrompt, trimmedPrompt);
            UserMessage userMessage = new UserMessage(combinedPrompt);
            
            Prompt prompt = new Prompt(List.of(userMessage));
            var response = chatModel.call(prompt);
            
            if (response == null || response.getResult() == null) {
                return "AI 파트너가 잠시 쉬고 있어요. 다시 불러주세요! 😥";
            }
            return response.getResult().getOutput().getContent();
        } catch (Exception e) {
            log.error("AI Error: {}", e.getMessage(), e);
            return "분석 중에 문제가 생겼어요. 잠시 후 다시 시도해 주세요. 💦";
        }
    }

    @Override
    public SemanticSearchResponse performSemanticSearch(String query) {
        // [검색] 사용자의 '개떡 같은' 질문도 '찰떡같이' 알아듣는 베테랑 중개사 모드
        String systemMsg = 
            "당신은 눈치 100단의 베테랑 공인중개사입니다. 사용자의 질문 속에 숨겨진 의도(출퇴근, 육아, 투자 등)를 파악하세요.\n" +
            "답변은 다음 형식을 엄수하세요:\n" +
            "1. ** 🧐 의도 파악**: 사용자의 상황에 깊이 공감하는 한 마디 (예: '신혼집을 찾으시는군요! 예쁘고 가성비 좋은 곳이 딱이죠.')\n" +
            "2. ** ✨ 추천 포인트**: 사용자가 원하는 조건이 왜 좋은지 3가지로 설명 (이모지 필수)\n" +
            "3. 맨 마지막 줄에 반드시 `[KEYWORDS: 키워드1, 키워드2, ...]` 형식을 추가하세요. (추출한 검색어)";
        
        String aiResponse = callGPT(query, systemMsg);
        
        List<ApartmentBasicInfo> searchResults = new ArrayList<>();
        String analysis = aiResponse;
        
        // 키워드 파싱 로직
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
                analysis = aiResponse.substring(0, aiResponse.lastIndexOf("[KEYWORDS:")).trim();
            } catch (Exception e) {
                log.error("Keyword parsing failed", e);
            }
        }

        return SemanticSearchResponse.builder()
                .results(searchResults.stream()
                        .map(info -> AddressResponse.builder()
                                .aptSeq(info.aptSeq())
                                .aptName(info.aptName())
                                .dongName(info.address())
                                .build())
                        .collect(Collectors.toList()))
                .analysis(analysis)
                .build();
    }

    @Override
    public ParseFilterResponse parseFilter(String query) {
        // [필터] 친절한 설명 + 정확한 JSON 데이터 추출
        String prompt = "사용자의 말을 듣고 검색 필터를 설정하려고 합니다. \n" +
                "1. 먼저 사용자의 요구사항을 '알겠습니다! ~조건으로 찾아볼게요' 처럼 친절하게 텍스트로 답변하세요.\n" +
                "2. 그 다음 줄에 오직 JSON 데이터만 출력하세요. (마크다운 ```json 없이)\n" +
                "JSON 형식: {\"priceRange\": {\"min\": 0, \"max\": 100}, \"areaRange\": {\"min\": 0, \"max\": 100}}\n" +
                "단위: 가격(억), 면적(평). 언급 없으면 기본값(0~1000) 사용.";
        
        String fullResponse = callGPT(query, "당신은 센스 있는 검색 도우미입니다.");
        
        FilterConditions filters;
        String analysisText = fullResponse;

        try {
            int jsonStart = fullResponse.indexOf("{");
            int jsonEnd = fullResponse.lastIndexOf("}");
            
            if (jsonStart != -1 && jsonEnd != -1) {
                String jsonPart = fullResponse.substring(jsonStart, jsonEnd + 1);
                analysisText = fullResponse.substring(0, jsonStart).trim(); // JSON 앞부분(친절한 멘트)만 사용자에게 보여줌
                filters = objectMapper.readValue(jsonPart, FilterConditions.class);
            } else {
                throw new RuntimeException("JSON not found");
            }
        } catch (Exception e) {
            log.warn("Filter parsing fallback", e);
            filters = FilterConditions.builder()
                    .priceRange(FilterConditions.PriceRange.builder().min(0).max(200).build())
                    .areaRange(FilterConditions.AreaRange.builder().min(0).max(100).build())
                    .build();
        }

        return ParseFilterResponse.builder()
                .filters(filters)
                .analysis(analysisText)
                .build();
    }

    @Override
    public FraudAnalysisResponse performFraudAnalysis(FraudAnalysisRequest request) {
        // [전세사기] 임차인 편에 선 든든한 해결사 모드
        String prompt = String.format(
                "매물 데이터: [주소: %s, 매매가: %d만원, 보증금: %d만원, 선순위채권: %d만원]\n" +
                "이 집이 안전한지 임차인 입장에서 아주 냉정하게 분석해주세요.\n" +
                "형식:\n" +
                "1. **🛡️ 안전 등급 진단**: (안전/주의/위험) 중 하나를 딱 잘라 말하고, 전세가율(매매가 대비 보증금+대출) %를 명시하세요.\n" +
                "2. **🕵️ 핵심 분석**: '집주인 빚이 집값의 80%%나 돼요!' 처럼 쉬운 말로 위험성을 경고하세요.\n" +
                "3. **💡 전문가의 조언**: 보증보험 가입 가능 여부나 특약 사항 등 실질적인 팁을 주세요.",
                request.getAddress(), request.getMarketValue(), request.getDeposit(), request.getPriorDebt());
        
        String analysis = callGPT(prompt, "당신은 임차인의 돈을 지켜주는 정의로운 권리분석 전문가입니다. 돌려 말하지 말고 직설적으로 조언하세요.");
        
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
        // [지역 분석] 20년 토박이 주민 모드 (가장 중요: 구체적 명사 사용 강제)
        
        String locationName = "지역코드 " + areaCode; // 기본값
        try {
            DongCodeResponse dongInfo = dongCodeMapper.selectByCode(areaCode);
            if (dongInfo != null) {
                // sidoName, gugunName, dongName을 조합하여 주소 생성
                locationName = String.format("%s %s %s", 
                    dongInfo.sidoName() != null ? dongInfo.sidoName() : "",
                    dongInfo.gugunName() != null ? dongInfo.gugunName() : "",
                    dongInfo.dongName() != null ? dongInfo.dongName() : "").trim();
            }
        } catch (Exception e) {
            log.warn("Address resolution failed for code: {}", areaCode);
        }

        String prompt = String.format(
                "분석 대상: %s, 아파트명 [%s].\n" +
                "이 정보를 바탕으로, 해당 동네에 20년 산 '토박이 주민' 입장에서 아주 구체적으로 자랑하듯 설명해주세요.\n" +
                "**[절대 규칙]**\n" +
                "- '지도를 확인하세요', '정확한 위치는' 같은 말 금지.\n" +
                "- 불확실하면 해당 '구'나 '동'의 유명한 랜드마크(공원, 백화점, 지하철역)를 예시로 드세요.\n" +
                "- 형식:\n" +
                "  🚇 **교통**: 가까운 지하철역 이름, 주요 도로망 언급.\n" +
                "  🏫 **학군**: 초/중/고 통학 환경이나 학원가 분위기.\n" +
                "  🌳 **인프라**: 마트, 공원, 병원 등 살기 좋은 이유.",
                locationName, apartmentName);
        
        return callGPT(prompt, "당신은 " + locationName + " 지역 정보를 꿰뚫고 있는 마당발 주민입니다. 친구에게 집을 소개하듯 신나게 설명해주세요.");
    }

    @Override
    public String getComparisonSummary(String comparisonData) {
        // [비교] 쇼핑 호스트 모드
        String prompt = "다음 매물들을 비교해달라는 요청입니다: " + comparisonData + "\n" +
                "사용자가 결정을 내리기 쉽게 '쇼핑 호스트' 톤으로 비교해주세요.\n" +
                "형식:\n" +
                "1. **⚖️ 한 줄 요약**: '가성비는 A가 좋지만, 몸이 편한 건 B네요!'\n" +
                "2. **💎 매물별 매력 포인트**: 각 매물의 장점을 콕 집어 설명.\n" +
                "3. **🙋 이런 분께 추천**: '신혼부부라면 A, 아이가 있다면 B를 추천해요!'";
        
        return callGPT(prompt, "당신은 결정장애를 해결해주는 명쾌한 쇼핑 호스트입니다.");
    }
}