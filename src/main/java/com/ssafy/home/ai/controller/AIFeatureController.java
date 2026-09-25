package com.ssafy.home.ai.controller;

import com.ssafy.home.ai.dto.ApartmentChatRequest;
import com.ssafy.home.ai.dto.ParseFilterResponse;
import com.ssafy.home.ai.exception.AIUnavailableException;
import com.ssafy.home.ai.service.ApartmentAiService;
import com.ssafy.home.ai.dto.SemanticSearchRequest;
import com.ssafy.home.ai.service.AIService;
import com.ssafy.home.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "AI 기능 API", description = "AI를 활용한 지능형 부동산 서비스 API")
public class AIFeatureController {

    private final AIService aiService;
    private final ApartmentAiService apartmentAiService;

    @Operation(summary = "자연어 필터 파싱", description = "자연어 질의로부터 지도 필터 조건을 파싱합니다")
    @PostMapping("/parse-filter")
    public ResponseEntity<CommonResponse<ParseFilterResponse>> parseFilter(@RequestBody SemanticSearchRequest request) {
        log.info("필터 파싱 요청: {}", request.getQuery());
        ParseFilterResponse response = aiService.parseFilter(request.getQuery());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "지역/매물 분석 리포트", description = "특정 매물이나 지역에 대한 AI 상세 분석 리포트를 생성합니다")
    @GetMapping("/analysis")
    public ResponseEntity<CommonResponse<String>> getAnalysis(
            @RequestParam String areaCode,
            @RequestParam String aptName) {
        String analysis = aiService.getRegionalAnalysis(areaCode, aptName);
        return ResponseEntity.ok(CommonResponse.success(analysis));
    }

    @Operation(summary = "매물 비교 요약", description = "관심 매물 목록에 대한 요약 비교 리포트를 생성합니다")
    @PostMapping("/comparison-summary")
    public ResponseEntity<CommonResponse<String>> getComparisonSummary(@RequestBody String comparisonData) {
        String summary = aiService.getComparisonSummary(comparisonData);
        return ResponseEntity.ok(CommonResponse.success(summary));
    }

    @Operation(summary = "AI 입지 매력 분석",
            description = "aptSeq를 주면 단지 정보 카드(실거래·주변 시설)를 근거로 요약한다. aptName·address만 주는 방식은 이전 Vue 화면 호환용")
    @GetMapping("/location-attraction")
    public ResponseEntity<CommonResponse<String>> analyzeLocationAttractiveness(
            @RequestParam(required = false) String aptSeq,
            @RequestParam(required = false) String aptName,
            @RequestParam(required = false) String address) {
        if (aptSeq != null && !aptSeq.isBlank()) {
            return grounded(() -> apartmentAiService.locationAttraction(aptSeq));
        }
        String analysis = aiService.analyzeLocationAttractiveness(aptName, address);
        return ResponseEntity.ok(CommonResponse.success(analysis));
    }

    @Operation(summary = "단지 AI 질문", description = "단지 정보 카드(실거래·주변 시설)와 이 단지의 실거래 조회를 근거로 질문에 답합니다")
    @PostMapping("/apartment-chat")
    public ResponseEntity<CommonResponse<String>> apartmentChat(@RequestBody ApartmentChatRequest request) {
        if (request.aptSeq() == null || request.aptSeq().isBlank() || request.message() == null || request.message().isBlank()) {
            return ResponseEntity.badRequest().body(CommonResponse.fail("400", "aptSeq와 message가 필요합니다."));
        }
        return grounded(() -> apartmentAiService.chat(request));
    }

    private ResponseEntity<CommonResponse<String>> grounded(java.util.function.Supplier<String> call) {
        try {
            return ResponseEntity.ok(CommonResponse.success(call.get()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(CommonResponse.fail("404", "단지 정보를 찾을 수 없습니다."));
        } catch (AIUnavailableException e) {
            if (e.isRateLimited()) {
                return ResponseEntity.status(429).body(CommonResponse.fail("429", "AI 사용량이 많아 잠시 후 다시 시도해 주세요. (약 1분)"));
            }
            return ResponseEntity.status(503).body(CommonResponse.fail("503", "AI 분석 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."));
        }
    }

}
