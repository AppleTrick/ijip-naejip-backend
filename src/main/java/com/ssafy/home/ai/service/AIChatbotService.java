package com.ssafy.home.ai.service;

import com.ssafy.home.ai.dto.SemanticSearchResponse;

/**
 * 데이터 기반 부동산 분석 어시스턴트
 * 사용자 질문을 받아 SQL을 생성하고 실행한 후 결과를 자연어로 해석
 */
public interface AIChatbotService {
    SemanticSearchResponse generateResponse(String userMessage);
}
