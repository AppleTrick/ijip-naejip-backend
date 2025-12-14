package com.ssafy.home.service;

import com.ssafy.home.dto.ApartmentDetailResponse;

public interface ApartmentService {
    /**
     * 아파트 상세 정보 조회
     * @param aptSeq 아파트 고유번호
     * @param pyung 평형 ("all" 또는 특정 평형)
     * @return 아파트 상세 정보
     */
    ApartmentDetailResponse getApartmentDetail(String aptSeq, String pyung);
}

