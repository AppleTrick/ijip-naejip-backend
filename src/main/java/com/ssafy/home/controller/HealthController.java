package com.ssafy.home.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "헬스 체크", description = "서버 상태 확인 API")
public class HealthController {

    @Operation(summary = "헬스 체크", description = "서버가 정상적으로 작동하고 있는지 확인합니다")
    @GetMapping("/")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "SSAFY Home API Server");
        return response;
    }
}

