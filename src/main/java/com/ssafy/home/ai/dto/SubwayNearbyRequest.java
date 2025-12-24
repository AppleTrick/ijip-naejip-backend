package com.ssafy.home.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 지하철역 인근 아파트 검색 요청 DTO
 */
public record SubwayNearbyRequest(
    @JsonPropertyDescription("검색할 지하철역 이름 (예: '강남역', '잠실역')")
    @JsonProperty(value = "stationName", required = true)
    String stationName,

    @JsonPropertyDescription("역에서의 최대 거리 (미터 단위, 기본값: 1000)")
    @JsonProperty("radiusMeters")
    Integer radiusMeters,

    @JsonPropertyDescription("최대 가격 (단위: 만원)")
    @JsonProperty("maxPrice")
    Integer maxPrice,

    @JsonPropertyDescription("최소 평수")
    @JsonProperty("minPyung")
    Integer minPyung
) {}
