package com.ssafy.home.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudAnalysisResponse {
    private String safetyGrade; // SAFE, WARNING, DANGER
    private String message;
    private int debtRatio;
}
