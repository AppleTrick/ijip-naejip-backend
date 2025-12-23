package com.ssafy.home.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterConditions {
    private PriceRange priceRange;
    private AreaRange areaRange;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PriceRange {
        private int min;
        private int max;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AreaRange {
        private int min;
        private int max;
    }
}
