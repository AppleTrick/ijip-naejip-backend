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
        return response;
    }
}

