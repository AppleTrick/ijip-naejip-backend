package com.ssafy.home.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 아파트 가격 동향 조회 요청 DTO
 */
public record PriceTrendRequest(
    @JsonPropertyDescription("아파트 이름 (예: '래미안' 또는 정확한 단지명)")
    @JsonProperty(required = true)
    String aptName,

    @JsonPropertyDescription("검색할 지역 (동, 구 등)")
    String region
) {}
