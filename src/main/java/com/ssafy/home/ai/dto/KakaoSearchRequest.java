package com.ssafy.home.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record KakaoSearchRequest(
    @JsonPropertyDescription("The search query for apartments (e.g., '서울 복층 아파트', '강남 테라스 아파트')")
    @JsonProperty(required = true)
    String query
) {}
