package com.ssafy.home.dto;

/**
 * 법정동 코드 정보 응답 DTO (불변 데이터 전송 객체)
 * 시도, 구군, 동의 계층적 구조와 지리정보를 반환합니다.
 */
public record DongCodeResponse(
        String dongCode,
        String sidoName,
        String gugunName,
        String dongName,
        Double latitude,
        Double longitude,
        Integer avgPrice
) {}

