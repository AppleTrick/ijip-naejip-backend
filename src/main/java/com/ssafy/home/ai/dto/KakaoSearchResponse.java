package com.ssafy.home.ai.dto;

import java.util.List;

public record KakaoSearchResponse(
    List<ApartmentInfo> apartments
) {
    public record ApartmentInfo(
        String name,
        String address,
        String description,
        Double latitude,
        Double longitude
    ) {}
}
