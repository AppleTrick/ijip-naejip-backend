package com.ssafy.home.ai.controller;

import com.ssafy.home.ai.dto.SemanticSearchRequest;
import com.ssafy.home.ai.dto.SemanticSearchResponse;
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
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI 기능 API", description = "AI를 활용한 지능형 부동산 서비스 API")
public class AIFeatureController {

    private final AIService aiService;

    @Operation(summary = "시맨틱 매물 검색", description = "자연어 질의를 통한 임베딩 기반 매물 검색을 수행합니다")
    @PostMapping("/search")
    public ResponseEntity<CommonResponse<SemanticSearchResponse>> semanticSearch(@RequestBody SemanticSearchRequest request) {
        log.info("시맨틱 검색 요청: {}", request.getQuery());
        SemanticSearchResponse response = aiService.performSemanticSearch(request.getQuery());
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
}
