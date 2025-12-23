package com.ssafy.home.ai.controller;

import com.ssafy.home.ai.dto.FraudAnalysisRequest;
import com.ssafy.home.ai.dto.FraudAnalysisResponse;
import com.ssafy.home.ai.dto.ParseFilterResponse;
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

    @Operation(summary = "자연어 필터 파싱", description = "자연어 질의로부터 지도 필터 조건을 파싱합니다")
    @PostMapping("/parse-filter")
    public ResponseEntity<CommonResponse<ParseFilterResponse>> parseFilter(@RequestBody SemanticSearchRequest request) {
        log.info("필터 파싱 요청: {}", request.getQuery());
        ParseFilterResponse response = aiService.parseFilter(request.getQuery());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "전세 사기 안전도 분석", description = "매물 정보를 바탕으로 AI가 전세 사기 위험도를 분석합니다")
    @PostMapping("/fraud-check")
    public ResponseEntity<CommonResponse<FraudAnalysisResponse>> fraudCheck(@RequestBody FraudAnalysisRequest request) {
        log.info("사기 분석 요청: {}", request.getAddress());
        FraudAnalysisResponse response = aiService.performFraudAnalysis(request);
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

    @Operation(summary = "AI 입지 매력 분석 (Stream)", description = "SSE 스트림으로 입지 분석 결과를 실시간으로 전송합니다")
    @GetMapping(value = "/location-attraction", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public reactor.core.publisher.Flux<String> analyzeLocationAttractiveness(
            @RequestParam String aptName,
            @RequestParam String address) {
        return aiService.analyzeLocationAttractiveness(aptName, address);
    }

    @Operation(summary = "문서(등기부등본) 분석", description = "업로드된 문서를 AI Vision으로 분석하여 보증금, 시세 등을 추출합니다")
    @PostMapping(value = "/analyze-document", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<com.ssafy.home.ai.dto.DocumentAnalysisResponse>> analyzeDocument(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        com.ssafy.home.ai.dto.DocumentAnalysisResponse response = aiService.analyzeDocument(file);
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
