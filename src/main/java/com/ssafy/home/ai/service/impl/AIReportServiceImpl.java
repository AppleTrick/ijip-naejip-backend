package com.ssafy.home.ai.service.impl;

import com.ssafy.home.ai.dto.AIReport;
import com.ssafy.home.ai.dto.AIReportResponse;
import com.ssafy.home.ai.dto.SemanticSearchResponse;
import com.ssafy.home.ai.mapper.AIReportMapper;
import com.ssafy.home.ai.service.AIReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIReportServiceImpl implements AIReportService {

    private final AIReportMapper aiReportMapper;

    @Override
    @Transactional
    public void saveReportAutomatically(Long userId, String query, SemanticSearchResponse response) {
        try {
            String title = generateTitle(query);

            AIReport report = AIReport.builder()
                    .userId(userId)
                    .title(title)
                    .query(query)
                    .markdownContent(response.getAnalysis())
                    .resultCount(response.getResults() != null ? response.getResults().size() : 0)
                    .build();

            aiReportMapper.insertReport(report);
            log.info("보고서 자동 저장 완료 - userId: {}, title: {}", userId, title);
        } catch (Exception e) {
            log.error("보고서 저장 실패 (무시하고 계속)", e);
            // 저장 실패해도 채팅 응답에는 영향 없음
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AIReportResponse> getReportsByUserId(Long userId) {
        log.info("보고서 목록 조회 - userId: {}", userId);

        List<AIReport> reports = aiReportMapper.findByUserId(userId);

        return reports.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AIReportResponse getReportById(Long userId, Long reportId) {
        log.info("보고서 상세 조회 - userId: {}, reportId: {}", userId, reportId);

        AIReport report = aiReportMapper.findById(reportId);

        if (report == null) {
            throw new IllegalArgumentException("보고서를 찾을 수 없습니다.");
        }

        if (!report.getUserId().equals(userId)) {
            throw new IllegalStateException("본인의 보고서만 조회할 수 있습니다.");
        }

        return convertToResponse(report);
    }

    @Override
    @Transactional
    public void deleteReport(Long userId, Long reportId) {
        log.info("보고서 삭제 - userId: {}, reportId: {}", userId, reportId);

        boolean exists = aiReportMapper.existsByIdAndUserId(reportId, userId);

        if (!exists) {
            throw new IllegalStateException("본인의 보고서만 삭제할 수 있습니다.");
        }

        aiReportMapper.deleteById(reportId);
    }

    private String generateTitle(String query) {
        // 질문에서 제목 자동 생성 (최대 50자)
        return query.length() > 50 ? query.substring(0, 47) + "..." : query;
    }

    private AIReportResponse convertToResponse(AIReport report) {
        return AIReportResponse.builder()
                .id(report.getId())
                .title(report.getTitle())
                .query(report.getQuery())
                .markdownContent(report.getMarkdownContent())
                .resultCount(report.getResultCount())
                .createdAt(report.getCreatedAt())
                .build();
    }
}

