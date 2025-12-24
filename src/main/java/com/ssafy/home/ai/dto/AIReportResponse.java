package com.ssafy.home.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 보고서 응답
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIReportResponse {
    private Long id;
    private String title;
    private String query;
    private String markdownContent;
    private Integer resultCount;
    private LocalDateTime createdAt;
}

