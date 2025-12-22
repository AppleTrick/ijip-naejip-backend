package com.ssafy.home.ai.service;

import com.ssafy.home.ai.dto.FraudAnalysisRequest;
import com.ssafy.home.ai.dto.FraudAnalysisResponse;
import com.ssafy.home.ai.dto.ParseFilterResponse;
import com.ssafy.home.ai.dto.SemanticSearchResponse;
import reactor.core.publisher.Flux;

public interface AIService {
    SemanticSearchResponse performSemanticSearch(String query);
    ParseFilterResponse parseFilter(String query);
    FraudAnalysisResponse performFraudAnalysis(FraudAnalysisRequest request);
    String getRegionalAnalysis(String areaCode, String apartmentName);
    String getComparisonSummary(String comparisonData);
    Flux<String> analyzeLocationAttractiveness(String aptName, String address);
    com.ssafy.home.ai.dto.DocumentAnalysisResponse analyzeDocument(org.springframework.web.multipart.MultipartFile file);
}
