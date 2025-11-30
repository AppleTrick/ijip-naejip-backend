package com.ssafy.home.controller;

import com.ssafy.home.dto.HouseDealResponse;
import com.ssafy.home.service.HouseDealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping({"/api/house", "/api/v1/house"})
@RequiredArgsConstructor
@Tag(name = "부동산 거래 API", description = "부동산 거래 내역 조회 API")
public class HouseDealController {

    private final HouseDealService houseDealService;

    @Operation(summary = "동코드별 최근 거래 내역 조회", description = "특정 법정동의 최근 거래 내역을 조회합니다")
    @GetMapping("/deals/dong/{dongCode}")
    public ResponseEntity<List<HouseDealResponse>> getDealsByDongCode(
            @Parameter(description = "법정동 코드 (예: 1168010100)", required = true)
            @PathVariable("dongCode") String dongCode,
            @Parameter(description = "조회 개수 (기본값: 10)")
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        log.info("동코드별 거래 내역 조회 요청 - 동코드: {}, 개수: {}", dongCode, limit);
        List<HouseDealResponse> deals = houseDealService.getRecentDealsByDongCode(dongCode, limit);
        log.info("동코드별 거래 내역 조회 완료: {} 개", deals.size());
        return ResponseEntity.ok(deals);
    }

    @Operation(summary = "아파트별 거래 내역 조회", description = "특정 아파트의 모든 거래 내역을 조회합니다")
    @GetMapping("/deals/apt/{aptSeq}")
    public ResponseEntity<List<HouseDealResponse>> getDealsByAptSeq(
            @Parameter(description = "아파트 시퀀스 (예: 11680-1)", required = true)
            @PathVariable("aptSeq") String aptSeq) {
        log.info("아파트별 거래 내역 조회 요청 - aptSeq: {}", aptSeq);
        List<HouseDealResponse> deals = houseDealService.getDealsByAptSeq(aptSeq);
        log.info("아파트별 거래 내역 조회 완료: {} 개", deals.size());
        return ResponseEntity.ok(deals);
    }

    @Operation(summary = "지도 영역별 거래 내역 조회 (deprecated)", description = "지도 영역(Bounds) 내의 거래 내역을 조회합니다 (deprecated - 사용 권장하지 않음)")
    @GetMapping("/deals/bounds")
    @Deprecated
    public ResponseEntity<List<HouseDealResponse>> getDealsByBounds(
            @Parameter(description = "최소 위도", required = true)
            @RequestParam("minLat") double minLat,
            @Parameter(description = "최대 위도", required = true)
            @RequestParam("maxLat") double maxLat,
            @Parameter(description = "최소 경도", required = true)
            @RequestParam("minLng") double minLng,
            @Parameter(description = "최대 경도", required = true)
            @RequestParam("maxLng") double maxLng,
            @Parameter(description = "조회 개수 (기본값: 100)")
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        log.info("지도 영역별 거래 내역 조회 요청 - minLat: {}, maxLat: {}, minLng: {}, maxLng: {}, limit: {}", minLat, maxLat, minLng, maxLng, limit);
        List<HouseDealResponse> deals = houseDealService.getHouseDealsByBounds(minLat, maxLat, minLng, maxLng, limit);
        log.info("지도 영역별 거래 내역 조회 완료: {} 개", deals.size());
        return ResponseEntity.ok(deals);
    }
}

