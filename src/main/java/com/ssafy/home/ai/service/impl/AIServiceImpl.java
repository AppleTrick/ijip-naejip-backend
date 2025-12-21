package com.ssafy.home.ai.service.impl;

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
    public String getRegionalAnalysis(String areaCode, String apartmentName) {
        return "해당 지역은 최근 GTX 호재로 인해 가치가 상승하고 있는 지역입니다. " + apartmentName + " 아파트는 주변 인프라가 잘 갖춰져 있어 투자 가치가 높습니다.";
    }

    @Override
    public String getComparisonSummary(String comparisonData) {
        return "선택하신 매물 중 첫 번째 매물이 가성비 면에서 가장 우수하며, 세 번째 매물은 넓은 실거주 면적을 선호하시는 분들께 적합합니다.";
    }
}
