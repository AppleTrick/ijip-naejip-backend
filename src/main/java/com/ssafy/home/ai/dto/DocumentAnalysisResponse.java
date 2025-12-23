package com.ssafy.home.ai.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentAnalysisResponse {
    private Long deposit;      // 보증금
    private Long marketValue;  // 매매 시세 (추정)
    private Long priorDebt;    // 선순위 채권 (근저당)
    private String address;    // 주소 (추출된 경우)
    private String summary;    // AI 분석 요약 (예: "을지로3가 등기부등본 확인됨")
}
