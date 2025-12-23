package com.ssafy.home.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LLM의 SQL 생성 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SqlGenerationResponse {

    /**
     * LLM의 추론 과정
     */
    private String reasoning;

    /**
     * 생성된 SQL 쿼리
     */
    private String sql;

    /**
     * 쿼리에 대한 자연어 설명
     */
    private String queryDescription;

    /**
     * SQL 생성 성공 여부
     */
    @Builder.Default
    private boolean isValid = false;
}

