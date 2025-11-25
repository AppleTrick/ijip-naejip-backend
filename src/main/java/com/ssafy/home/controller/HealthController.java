package com.ssafy.home.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    /**
     * 헬스 체크 API
     * GET /
     *
     * @return 서버 상태 정보
     */
    @GetMapping("/")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "SSAFY Home API Server");
        response.put("message", "MyBatis 기반 부동산 실거래가 조회 API가 정상 동작 중입니다.");
        response.put("endpoints", new String[]{
            "GET /api/dongcodes/sido - 시도 목록 조회",
            "GET /api/dongcodes/gugun?sido={시도명} - 구군 목록 조회",
            "GET /api/dongcodes/dong?sido={시도명}&gugun={구군명} - 동 목록 조회",
            "GET /api/house-deals/dong/{dongCode}?limit=100 - 특정 동의 최근 실거래가 조회",
            "GET /api/house-deals/apt/{aptSeq} - 특정 아파트의 실거래가 조회",
            "GET /api/house-deals/{no} - 거래번호로 실거래가 상세 조회"
        });
        return response;
    }
}

