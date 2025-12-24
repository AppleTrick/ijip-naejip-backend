package com.ssafy.home.ai.service.impl;

import com.ssafy.home.ai.dto.*;
import com.ssafy.home.ai.service.AIService;
import com.ssafy.home.mapper.ApartmentMapper;
import com.ssafy.home.mapper.DongCodeMapper;
import com.ssafy.home.dto.DongCodeResponse;
import com.ssafy.home.dto.AddressResponse;
import com.ssafy.home.dto.mapper.ApartmentBasicInfo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AIServiceImpl implements AIService {

    private final ChatModel chatModel;
    private final ChatClient chatClient;
    private final ApartmentMapper apartmentMapper;
    private final DongCodeMapper dongCodeMapper;

    public AIServiceImpl(ChatModel chatModel, ChatClient.Builder chatClientBuilder, 
                        ApartmentMapper apartmentMapper, DongCodeMapper dongCodeMapper) {
        this.chatModel = chatModel;
        this.chatClient = chatClientBuilder.build();
        this.apartmentMapper = apartmentMapper;
        this.dongCodeMapper = dongCodeMapper;
    }
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();


    
    /**
     * 대괄호 짝 찾기
     */
    private int findMatchingBracket(String s, int openIdx) {
        if (openIdx < 0 || openIdx >= s.length() || s.charAt(openIdx) != '[') return -1;
        int depth = 0;
        for (int i = openIdx; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }



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
            String combinedPrompt = String.format("---[지시사항]---\n%s \n---[사용자 입력]---\n%s", globalSystemPrompt, trimmedPrompt);
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
        log.info("Performing Semantic Search with Tool Calling for query: {}", query);

        String systemMsg = 
            "당신은 부동산 전문가로서 사용자의 질문에 즉시 답하는 검색 봇입니다.\n" +
            "\n" +
            "**[금지 사항 - 매우 중요]**\n" +
            "- '검색해 보겠습니다', '확인해 보니...', '죄송하지만...' 같은 안내 문구는 절대 하지 마세요.\n" +
            "- 도구 호출을 위해 사용자에게 질문하거나 허락을 구하지 마세요.\n" +
            "- 아파트 매물의 점수를 매기거나 순위를 정하지 말고 객관적인 사실 기반으로 추천 이유를 설명하세요.\n" +
            "\n" +
            "**[검색 지침]**\n" +
            "1. **지하철역 기반**: '강남역', '홍대입구역' 등 역 이름이 있으면 무조건 `subwayNearbyFunction`을 사용하세요.\n" +
            "2. **조건 부합**: 2인 가구용은 15~25평(50~85㎡), 조용한 곳은 주거 단지 비중이 높은 곳을 우선하세요.\n" +
            "3. **결과 출처**: 반드시 검색 도구가 반환한 결과만 사용하고, 결과가 없으면 솔직하게 없다고 답하세요.\n" +
            "\n" +
            "**[출력 형식]**\n" +
            "🧐 소견: [추천 아파트들의 특징과 선정 이유 (간결하게 한글로)]\n" +
            "\n" +
            "JSON_RESULTS: [{\"name\": \"...\", \"address\": \"...\", \"lat\": ..., \"lng\": ...}]";

        try {
            String aiResponse = chatClient.prompt()
                    .system(systemMsg)
                    .user(query)
                    .functions("localSearchFunction", "kakaoSearchFunction", "subwayNearbyFunction", "priceTrendFunction")
                    .call()
                    .content();

            if (aiResponse == null) {
                return SemanticSearchResponse.builder()
                        .analysis("AI 파트너가 응답하지 않았어요. 잠시 후 다시 시도해 주세요.")
                        .results(new ArrayList<>())
                        .build();
            }

            log.info("AI Response with tools (raw): {}", aiResponse);
            
            List<AddressResponse> resultList = new ArrayList<>();
            String analysis = aiResponse;

            // JSON_RESULTS 파싱 (다양한 형식 지원)
            try {
                // 유니코드 이스케이프 디코딩
                String decodedResponse = aiResponse;
                
                // JSON 배열 찾기: JSON_RESULTS 이후 또는 마지막 [ ] 블록
                String jsonPart = null;
                
                // 패턴 1: JSON_RESULTS: [...]
                int jsonResultsIdx = decodedResponse.indexOf("JSON_RESULTS");
                if (jsonResultsIdx != -1) {
                    int arrayStart = decodedResponse.indexOf("[", jsonResultsIdx);
                    if (arrayStart != -1) {
                        int arrayEnd = findMatchingBracket(decodedResponse, arrayStart);
                        if (arrayEnd != -1) {
                            jsonPart = decodedResponse.substring(arrayStart, arrayEnd + 1);
                        }
                    }
                }
                
                // 패턴 2: 폴백 - 가장 마지막에 있는 [ ] 배열 찾기 (AI가 형식을 어겼을 때 대비)
                if (jsonPart == null) {
                    int lastOpen = decodedResponse.lastIndexOf("[");
                    if (lastOpen != -1) {
                        int lastClose = decodedResponse.lastIndexOf("]");
                        if (lastClose > lastOpen) {
                            jsonPart = decodedResponse.substring(lastOpen, lastClose + 1);
                        }
                    }
                }
                
                // 패턴 2: 마지막 JSON 배열 찾기 ([ 로 시작하고 ] 로 끝나는)
                if (jsonPart == null) {
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\[\\s*\\{[^\\[]*\\}\\s*\\]", java.util.regex.Pattern.DOTALL);
                    java.util.regex.Matcher matcher = pattern.matcher(decodedResponse);
                    String lastMatch = null;
                    while (matcher.find()) {
                        lastMatch = matcher.group();
                    }
                    jsonPart = lastMatch;
                }
                
                if (jsonPart != null) {
                    log.info("Found JSON part: {}", jsonPart.length() > 200 ? jsonPart.substring(0, 200) + "..." : jsonPart);
                    
                    com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(jsonPart);
                    if (rootNode.isArray()) {
                        for (com.fasterxml.jackson.databind.JsonNode node : rootNode) {
                            if (!node.has("name")) continue;
                            String name = node.get("name").asText();
                            String address = node.has("address") ? node.get("address").asText() : "";
                            double lat = node.has("lat") ? node.get("lat").asDouble() : 0.0;
                            double lng = node.has("lng") ? node.get("lng").asDouble() : 0.0;

                            // DB 매칭 시도
                            List<ApartmentBasicInfo> dbResults = apartmentMapper.searchByAptNames(List.of(name));
                            
                            if (!dbResults.isEmpty()) {
                                ApartmentBasicInfo dbInfo = dbResults.get(0);
                                log.info("Matched with DB: {}", name);
                                resultList.add(AddressResponse.builder()
                                        .aptSeq(dbInfo.aptSeq())
                                        .aptName(dbInfo.aptName())
                                        .dongName(dbInfo.address())
                                        .latitude(dbInfo.latitude())
                                        .longitude(dbInfo.longitude())
                                        .build());
                            } else if (lat != 0.0 && lng != 0.0) {
                                // DB에 없더라도 카카오 정보를 바탕으로 결과 추가 (Fallback)
                                log.info("No DB match, using fallback: {}", name);
                                resultList.add(AddressResponse.builder()
                                        .aptSeq("EXT-" + name.hashCode())
                                        .aptName(name)
                                        .dongName(address)
                                        .latitude(lat)
                                        .longitude(lng)
                                        .build());
                            }
                        }
                    }

                    // 결과가 있으면 부정적인 멘트 정제
                    if (!resultList.isEmpty()) {
                        if (analysis.startsWith("죄송합니다") || analysis.contains("찾을 수 없습니다")) {
                            analysis = "🧐 소견: 요청하신 조건에 맞는 아파트들을 찾았습니다. 강남역 일대의 주거 환경과 편의성을 고려한 추천 목록입니다.";
                        }
                        // JSON_RESULTS 부분 제거하여 analysis 텍스트만 남김
                        int jsonStart = analysis.indexOf("JSON_RESULTS");
                        if (jsonStart != -1) {
                            analysis = analysis.substring(0, jsonStart).trim();
                        }
                    }
                    log.info("Total results: {} (DB: {}, Fallback: {})", 
                        resultList.size(), 
                        resultList.stream().filter(r -> !r.getAptSeq().startsWith("KAKAO-")).count(),
                        resultList.stream().filter(r -> r.getAptSeq().startsWith("KAKAO-")).count());
                    }
                } else {
                    log.warn("No JSON_RESULTS found in response");
                }
            } catch (Exception e) {
                log.error("JSON_RESULTS parsing failed: {}", e.getMessage());
            }

            return SemanticSearchResponse.builder()
                    .results(resultList)
                    .analysis(analysis)
                    .build();

        } catch (Exception e) {
            log.error("Semantic Search Tool Error: {}", e.getMessage(), e);
            return SemanticSearchResponse.builder()
                    .analysis("분석 중 오류가 발생했습니다: " + e.getMessage())
                    .results(new ArrayList<>())
                    .build();
        }
    }

    @Override
    public ParseFilterResponse parseFilter(String query) {
        // [필터] 속도 최적화: globalSystemPrompt를 태우지 않고 가볍게 처리
        String systemInstruction = "You are a precise data extractor. Your goal is to convert natural language queries into a JSON object.\n" +
                "Response Format:\n" +
                "Line 1: A polite, single-sentence confirmation in Korean (e.g., '네, 30평대 5억 이하 매물을 찾아보겠습니다.').\n" +
                "Line 2: The JSON object ONLY.\n" +
                "Rules:\n" +
                "- JSON keys: priceRange (min, max in 억), areaRange (min, max in 평)\n" +
                "- Default: 0 to 1000 if not specified.\n" +
                "- Do NOT explain or ask questions. JUST output the two lines.";

        String userPrompt = String.format("Request: \"%s\"", query);
        
        // 직접 ChatModel 호출 (callGPT 우회)
        String fullResponse = "";
        try {
            Prompt prompt = new Prompt(List.of(
                new org.springframework.ai.chat.messages.SystemMessage(systemInstruction),
                new UserMessage(userPrompt)
            ));
            var response = chatModel.call(prompt);
            if (response != null && response.getResult() != null) {
                fullResponse = response.getResult().getOutput().getContent();
            }
        } catch (Exception e) {
            log.error("AI Fast Search Error", e);
            fullResponse = "검색 조건을 확인하는 중 오류가 발생했습니다.\n{}";
        }
        
        FilterConditions filters;
        String analysisText = fullResponse;

        try {
            int jsonStart = fullResponse.indexOf("{");
            int jsonEnd = fullResponse.lastIndexOf("}");
            
            if (jsonStart != -1 && jsonEnd != -1) {
                String jsonPart = fullResponse.substring(jsonStart, jsonEnd + 1);
                analysisText = fullResponse.substring(0, jsonStart).trim();
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
                "1. **줄바꿈 필수**: 각 항목(교통, 학군, 인프라) 사이에는 반드시 빈 줄을 넣어 구분하세요.\n" +
                "2. '지도를 확인하세요', '정확한 위치는' 같은 말 금지.\n" +
                "3. 불확실하면 해당 '구'나 '동'의 유명한 랜드마크(공원, 백화점, 지하철역)를 예시로 드세요.\n" +
                "- 형식:\n" +
                "  🚇 교통: \n" +
                "     - 가까운 지하철역 이름, 주요 도로망 언급.\n\n" +
                "  🏫 학군: \n" +
                "     - 초/중/고 통학 환경이나 학원가 분위기.\n\n" +
                "  🌳 인프라: \n" +
                "     - 마트, 공원, 병원 등 살기 좋은 이유.",
                locationName, apartmentName);
        
        return callGPT(prompt, "당신은 " + locationName + " 지역 정보를 꿰뚫고 있는 마당발 주민입니다. 친구에게 집을 소개하듯 신나게 설명해주되, 가독성을 위해 줄바꿈을 적극적으로 사용하세요.");
    }

    @Override
    public String getComparisonSummary(String comparisonData) {
        // [비교] 쇼핑 호스트 + 데이터 분석가 모드
        String prompt = "다음 매물들을 비교분석해주세요: " + comparisonData + "\n\n" +
                "**[응답 필수 형식 - 반드시 JSON으로만 응답하세요]**\n" +
                "{\n" +
                "  \"summary\": \"마크다운 형식의 상세 분석 리포트\",\n" +
                "  \"scores\": [\n" +
                "    {\n" +
                "      \"name\": \"아파트 이름\",\n" +
                "      \"transportation\": 1~10 점수,\n" +
                "      \"education\": 1~10 점수,\n" +
                "      \"convenience\": 1~10 점수,\n" +
                "      \"environment\": 1~10 점수,\n" +
                "      \"futureValue\": 1~10 점수\n" +
                "    }\n" +
                "  ]\n" +
                "}\n\n" +
                "**[분석 가이드]**\n" +
                "1. summary에는 쇼핑 호스트처럼 친절하고 명쾌한 비교 분석 내용을 담으세요. (이모지, 불렛포인트 활용)\n" +
                "2. scores에는 각 아파트의 특징을 5개 지표(교통, 학군, 편리사성, 환경, 가치)로 수치화하세요.\n" +
                "3. 데이터가 부족하더라도 당신의 지식을 바탕으로 가장 합리적인 가상의 점수를 부여하세요.";
        
        return callGPT(prompt, "당신은 냉철한 데이터 분석가이자 설득력 있는 쇼핑 호스트입니다. 반드시 요청한 JSON 규격을 엄수하세요.");
    }

    @Override
    public String analyzeLocationAttractiveness(String aptName, String address) {
        String prompt = String.format(
            "분석 대상: [%s] (주소: %s)\n" +
            "이 단지의 입지적 매력 3가지를 '✨ [키워드]: 설명' 형식으로 아주 짧고 명쾌하게 분석해 주세요.\n" +
            "**[출력 규칙]**\n" +
            "1. 반드시 3줄로 작성하세요.\n" +
            "2. 각 줄은 ✨ 이모지로 시작하세요.\n" +
            "3. 줄바꿈을 포함하여 가독성 있게 응답하세요.",
            aptName, address
        );
        
        return callGPT(prompt, "당신은 요점만 콕 짚어주는 입지 분석 전문가입니다.");
    }



}