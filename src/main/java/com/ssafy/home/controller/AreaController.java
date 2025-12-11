package com.ssafy.home.controller;

import com.ssafy.home.dto.*;
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
            @RequestParam(value = "minLat") Double minLat,
            @RequestParam(value = "maxLat") Double maxLat,
            @RequestParam(value = "minLng") Double minLng,
            @RequestParam(value = "maxLng") Double maxLng,
            @RequestParam(value = "scope") AreaScope scope,
            @RequestParam(value = "minPrice", required = false) Integer minPrice,
            @RequestParam(value = "maxPrice", required = false) Integer maxPrice,
            @RequestParam(value = "minPyung", required = false) Integer minPyung,
            @RequestParam(value = "maxPyung", required = false) Integer maxPyung
        ) {

        boolean isSearchForApt = (scope == AreaScope.APT || scope == AreaScope.APT_DONG);
        boolean isSearchWithPyung = (minPyung != null || maxPyung != null);

        if (!isSearchForApt && isSearchWithPyung) {
            return ResponseEntity.badRequest().body(
                CommonResponse.fail("400", "평수 필터는 아파트(APT) 또는 아파트 동(APT_DONG) 범위에서만 사용할 수 있습니다.")
            );
        }

        GeoBoundParam geoBoundParam = new GeoBoundParam(minLat, maxLat, minLng, maxLng);
        PriceRangeParam priceRangeParam = new PriceRangeParam(minPrice, maxPrice);
        PyungRangeParam pyungRangeParam = new PyungRangeParam(minPyung, maxPyung);
        List<AddressResponse> areas = areaService.searchAreaAddress(geoBoundParam, scope, priceRangeParam, pyungRangeParam);
        return ResponseEntity.ok(CommonResponse.success(areas));
    }
}
