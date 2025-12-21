package com.ssafy.home.ai.service.impl;

import com.ssafy.home.ai.dto.FilterConditions;
import com.ssafy.home.ai.dto.FraudAnalysisRequest;
import com.ssafy.home.ai.dto.FraudAnalysisResponse;
import com.ssafy.home.ai.dto.ParseFilterResponse;
import com.ssafy.home.ai.dto.SemanticSearchResponse;
import com.ssafy.home.ai.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    @Override
    public SemanticSearchResponse performSemanticSearch(String query) {
        log.info("Performing semantic search for query: {}", query);
        
        // 간단한 키워드 추출 시뮬레이션
        String analysis = "GPT-5-nano 분석: '" + query + "'에 대한 검색 결과입니다. " +
                "해당 지역의 교통 편의성과 주거 쾌적성을 고려하여 추천 목록을 구성했습니다.";

        return SemanticSearchResponse.builder()
                .results(new ArrayList<>()) // 실제로는 여기서 지역 정보를 바탕으로 검색 결과를 채웁니다.
                .analysis(analysis)
                .build();
    }

    @Override
    public ParseFilterResponse parseFilter(String query) {
        log.info("Parsing filter for query: {}", query);
        
        // Mock logic: In a real app, use GPT to extract structured filters.
        FilterConditions filters = FilterConditions.builder()
                .priceRange(FilterConditions.PriceRange.builder().min(0).max(15).build())
                .areaRange(FilterConditions.AreaRange.builder().min(20).max(40).build())
                .build();

        String analysis = "GPT-5-nano 분석 결과: '" + query + "'를 분석하여 매매가 15억 이하, 면적 20~40평형 필터를 적용했습니다.";

        return ParseFilterResponse.builder()
                .filters(filters)
                .analysis(analysis)
                .build();
    }

    @Override
    public FraudAnalysisResponse performFraudAnalysis(FraudAnalysisRequest request) {
        log.info("Performing fraud analysis for address: {}", request.getAddress());
        
        int debtRatio = 0;
        if (request.getMarketValue() > 0) {
            debtRatio = (int) (((request.getDeposit() + request.getPriorDebt()) * 100) / request.getMarketValue());
        }

        String grade = "SAFE";
        String message = "해당 매물은 부채 비율이 낮고 권리 관계가 깨끗하여 안전한 것으로 분석됩니다.";

        if (request.isViolation()) {
            grade = "DANGER";
            message = "위반건축물로 등록되어 있어 위험합니다. 전세보증보험 가입이 불가능할 수 있습니다.";
        } else if (debtRatio >= 80) {
            grade = "DANGER";
            message = "부채 비율이 " + debtRatio + "%로 매우 높아 '깡통전세' 위험이 큽니다.";
        } else if (debtRatio >= 70) {
            grade = "WARNING";
            message = "부채 비율이 " + debtRatio + "%로 다소 높습니다. 보증보험 가입을 반드시 권장합니다.";
        }

        return FraudAnalysisResponse.builder()
                .safetyGrade(grade)
                .message("GPT-5-nano 정밀 진단: " + message)
                .debtRatio(debtRatio)
                .build();
    }

    @Override
    public String getRegionalAnalysis(String areaCode, String apartmentName) {
        return "해당 지역은 최근 GTX 호재로 인해 가치가 상승하고 있는 지역입니다. " + apartmentName + " 아파트는 주변 인프라가 잘 갖춰져 있어 투자 가치가 높습니다.";
    }

    @Override
    public String getComparisonSummary(String comparisonData) {
        return "선택하신 매물 중 첫 번째 매물이 가성비 면에서 가장 우수하며, 세 번째 매물은 넓은 실거주 면적을 선호하시는 분들께 적합합니다.";
    }
}
