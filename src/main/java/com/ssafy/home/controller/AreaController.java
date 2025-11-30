package com.ssafy.home.controller;

import com.ssafy.home.dto.AreaScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/area")
@Slf4j
public class AreaController {
    /**
     * 특정 범위의 지역 정보 조회
     * GET /api/v1/area
     */
    @GetMapping
    public ResponseEntity<?> searchAreaAddress(
            @RequestParam(value = "minLat", required = false) Double minLat,
            @RequestParam(value = "maxLat", required = false) Double maxLat,
            @RequestParam(value = "minLng", required = false) Double minLng,
            @RequestParam(value = "maxLng", required = false) Double maxLng,
            @RequestParam(value = "scope", required = false) AreaScope scope) {
        boolean isLatLgnMissing = minLat == null || maxLat == null || minLng == null || maxLng == null;

        if (isLatLgnMissing) {
            log.warn("좌표 파라미터가 불완전합니다.");
            return ResponseEntity.badRequest().body("4개의 좌표(minLat, maxLat, minLng, maxLng)는 모두 입력되어야 합니다.");
        }

        return ResponseEntity.internalServerError().body("구현 중입니다.");
    }
}
