package com.ssafy.home.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 지역 정보 응답 DTO (불변 데이터 전송 객체)
 * 지도 범위 내 지역 조회 결과를 반환합니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private String dongCode;        // full 10-digit legal dong code
    private String sidoName;
    private String gugunName;
    private String dongName;

    private String aptSeq;
    private String aptName;
    private String aptDong;

    private Double latitude;
    private Double longitude;
    private Integer avgPrice;
    private Integer primaryPyung;
}

