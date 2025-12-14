package com.ssafy.home.dto;

public record GeoBoundParam(
    Double minLat,
    Double maxLat,
    Double minLng,
    Double maxLng
) { }
