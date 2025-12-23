package com.ssafy.home.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudAnalysisRequest {
    private String address;
    private long deposit;
    private long marketValue;
    private long priorDebt;
    private boolean isViolation;
    private String usage;
}
