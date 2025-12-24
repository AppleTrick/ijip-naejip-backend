package com.ssafy.home.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 분석 보고서 엔티티
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIReport {
    private Long id;
    private Long userId;
    private String title;
    private String query;
    private String markdownContent;
    private Integer resultCount;
    private LocalDateTime createdAt;
}
