package com.ssafy.home.controller;

import com.ssafy.home.dto.*;
import com.ssafy.home.service.AreaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/area")
@Slf4j
@Tag(name = "Area API (지역 검색)", description = "화면(지도) 기반 아파트 및 지역 정보 조회 API")
public class AreaController {
    private final AreaService areaService;

    @Operation(
        summary = "사각형 범위 기반 지역 목록 조회",
        description = "사각형 좌표 범위에 따른 지역 목록을 조회합니다.\n" +
                "- DONG/GUGUN/SIDO 범위: minPrice/maxPrice는 평당가(만원) 기준\n" +
                "- APT/APT_DONG 범위: minPrice/maxPrice는 총 거래가(만원) 기준"
    )
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

        GeoBoundParam geoBoundParam = new GeoBoundParam(minLat, maxLat, minLng, maxLng);
        PriceRangeParam priceRangeParam = isSearchForApt ? new PriceRangeParam(minPrice, maxPrice) : null;
        PyungRangeParam pyungRangeParam = isSearchForApt ? new PyungRangeParam(minPyung, maxPyung) : null;
        List<AddressResponse> areas = areaService.searchAreaAddress(geoBoundParam, scope, priceRangeParam, pyungRangeParam);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePublic())
                .body(CommonResponse.success(areas));
    }
}
