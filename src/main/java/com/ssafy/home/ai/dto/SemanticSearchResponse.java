package com.ssafy.home.ai.dto;

import com.ssafy.home.dto.AddressResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SemanticSearchResponse {
    private List<AddressResponse> results;
    private String analysis;
}
