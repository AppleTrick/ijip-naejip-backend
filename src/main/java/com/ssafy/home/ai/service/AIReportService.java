package com.ssafy.home.ai.service;

import com.ssafy.home.ai.dto.AIReportResponse;
import com.ssafy.home.ai.dto.SemanticSearchResponse;

import java.util.List;

/**
 * AI 보고서 자동 저장 및 조회 서비스
 */
public interface AIReportService {

    /**
     * 채팅 결과를 보고서로 자동 저장
     */
    void saveReportAutomatically(Long userId, String query, SemanticSearchResponse response);

    /**
     * 사용자별 보고서 목록 조회 (최신순)
     */
    List<AIReportResponse> getReportsByUserId(Long userId);

    /**
     * 보고서 상세 조회
     */
    AIReportResponse getReportById(Long userId, Long reportId);

    /**
     * 보고서 삭제
     */
    void deleteReport(Long userId, Long reportId);
}

