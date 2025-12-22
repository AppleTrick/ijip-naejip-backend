package com.ssafy.home.dto.mapper;

public record ApartmentBasicInfo(
    String aptSeq,
    String aptName,
    String address,
    Integer avgPrice,
    Integer buildYear,
    String latitude,
    String longitude
) {
}

