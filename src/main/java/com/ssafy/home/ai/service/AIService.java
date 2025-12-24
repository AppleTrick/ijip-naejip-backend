package com.ssafy.home.ai.service;

import com.ssafy.home.ai.dto.ParseFilterResponse;
import com.ssafy.home.ai.dto.SemanticSearchResponse;

public interface AIService {
    SemanticSearchResponse performSemanticSearch(String query);
    ParseFilterResponse parseFilter(String query);
    String getRegionalAnalysis(String areaCode, String apartmentName);
    String getComparisonSummary(String comparisonData);
    String analyzeLocationAttractiveness(String aptName, String address);
}
