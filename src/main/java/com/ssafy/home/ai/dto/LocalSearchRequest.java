package com.ssafy.home.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LocalSearchRequest(
    @JsonPropertyDescription("행정구역명 키워드 리스트 (예: ['역삼동', '강남구'])")
    @JsonProperty(required = true)
    List<String> keywords,

    @JsonPropertyDescription("최소 가격 (단위: 만원)")
    Integer minPrice,

    @JsonPropertyDescription("최대 가격 (단위: 만원)")
    Integer maxPrice,

    @JsonPropertyDescription("최소 평수")
    Integer minPyung,

    @JsonPropertyDescription("최대 평수")
    Integer maxPyung
) {}
