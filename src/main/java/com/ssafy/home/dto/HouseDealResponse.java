package com.ssafy.home.dto;

/**
 * 부동산 거래 정보 응답 DTO (불변 데이터 전송 객체)
 * 거래 내역과 아파트 정보를 포함한 조인된 데이터를 반환합니다.
 */
public record HouseDealResponse(
        String dongName,
        String gugunName,
        String sidoName,
        String dongCode,
        String longitude,
        String latitude,
        Integer buildYear,
        String jibun,
        String roadNm,
        String aptNm,
        Integer dealAmount,
        Double excluUseAr,
        Integer dealDate,
        String floor,
        String aptDong,
        String aptSeq,
        Integer no
) {}
