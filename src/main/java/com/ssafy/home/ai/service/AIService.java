package com.ssafy.home.ai.service;

import com.ssafy.home.ai.dto.SemanticSearchResponse;

public interface AIService {
    SemanticSearchResponse performSemanticSearch(String query);
    String getRegionalAnalysis(String areaCode, String apartmentName);
    String getComparisonSummary(String comparisonData);
}
