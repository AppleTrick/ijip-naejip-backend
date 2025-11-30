package com.ssafy.home.controller;

import com.ssafy.home.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "헬스 체크", description = "서버 상태 확인 API")
public class HealthController {

    @Operation(summary = "헬스 체크", description = "서버가 정상적으로 작동하고 있는지 확인합니다")
    @GetMapping("/")
    public ResponseEntity<CommonResponse<Map<String, Object>>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("service", "SSAFY Home API Server");
        return ResponseEntity.ok(CommonResponse.success("서버가 정상 작동 중입니다.", data));
    }
}

