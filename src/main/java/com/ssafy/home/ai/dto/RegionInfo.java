package com.ssafy.home.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 검증된 지역 정보 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionInfo {
    private String dongCode;
    private String sidoName;
    private String gugunName;
    private String dongName;
    private String fullAddress;  // 전체 주소 문자열
}

