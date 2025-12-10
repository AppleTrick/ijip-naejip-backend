package com.ssafy.home.controller;

import com.ssafy.home.dto.AddressResponse;
import com.ssafy.home.dto.AreaScope;
import com.ssafy.home.dto.CommonResponse;
import com.ssafy.home.service.AreaService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/area")
@Slf4j
public class AreaController {
    private final AreaService areaService;

    @Operation(summary = "사각형 범위 기반 지역 목록 조회", description = "사각형 좌표 범위에 따른 지역 목록을 조회합니다")
    @GetMapping
    public ResponseEntity<CommonResponse<List<AddressResponse>>> searchAreaAddress(
            @RequestParam(value = "minLat", required = false) Double minLat,
            @RequestParam(value = "maxLat", required = false) Double maxLat,
            @RequestParam(value = "minLng", required = false) Double minLng,
            @RequestParam(value = "maxLng", required = false) Double maxLng,
            @RequestParam(value = "scope", required = false) AreaScope scope,
            @RequestParam(value = "minPrice", required = false) Integer minPrice,
            @RequestParam(value = "maxPrice", required = false) Integer maxPrice,
            @RequestParam(value = "minPyung", required = false) Integer minPyung,
            @RequestParam(value = "maxPyung", required = false) Integer maxPyung
            ) {
        boolean isLatLgnMissing = minLat == null || maxLat == null || minLng == null || maxLng == null;

        if (isLatLgnMissing) {
            return ResponseEntity.badRequest()
                    .body(CommonResponse.fail("400", "4개의 좌표(minLat, maxLat, minLng, maxLng)는 모두 입력되어야 합니다."));
        }

        List<AddressResponse> areas = areaService.searchAreaAddress(minLat, maxLat, minLng, maxLng, scope);
        return ResponseEntity.ok(CommonResponse.success(areas));
    }
}
