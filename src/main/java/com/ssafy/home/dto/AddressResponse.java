package com.ssafy.home.dto;

/**
 * 지역 정보 응답 DTO (불변 데이터 전송 객체)
 * 지도 범위 내 지역 조회 결과를 반환합니다.
 */
public record AddressResponse(
        String dongCode,        // full 10-digit legal dong code
        String sidoName,
        String gugunName,
        String dongName,

        String aptSeq,
        String aptName,
        String aptDong,

        Double latitude,
        Double longitude,
        Integer avgPrice
) {}

