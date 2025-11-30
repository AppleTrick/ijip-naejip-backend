package com.ssafy.home.controller;

import com.ssafy.home.dto.CommonResponse;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "부동산 거래 및 아파트 정보 API", description = "부동산 거래 내역 및 아파트 정보 조회 API")
public class HouseDealController {
    private final HouseDealService houseDealService;

    @Operation(summary = "최근 거래 내역 조회", description = "최근 거래 내역을 조회합니다")
    @GetMapping("/deals")
    public ResponseEntity<CommonResponse<List<HouseDealResponse>>> getDeals(
            @Parameter(description = "법정동 코드 (10자리)")
            @RequestParam(value = "dongCode", required = false) String dongCode,
            @Parameter(description = "조회 개수 (기본값: 10)")
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        log.info("동코드별 거래 내역 조회 요청 - 동코드: {}, 개수: {}", dongCode, limit);
        List<HouseDealResponse> deals = houseDealService.getRecentDeals(dongCode, limit);
        log.info("동코드별 거래 내역 조회 완료: {} 개", deals.size());
        return ResponseEntity.ok(CommonResponse.success(deals));
    }

    // 2. 특정 아파트의 하위 리소스 (거래 내역) 조회
    @Operation(summary = "아파트별 거래 내역 조회", description = "특정 아파트의 모든 거래 내역을 조회합니다")
    @GetMapping("/apartments/{aptSeq}/deals")
    public ResponseEntity<CommonResponse<List<HouseDealResponse>>> getDealsByAptSeq(
            @Parameter(description = "아파트 시퀀스 (예: 11680-1)", required = true)
            @PathVariable("aptSeq") String aptSeq) {
        log.info("아파트별 거래 내역 조회 요청 - aptSeq: {}", aptSeq);
        List<HouseDealResponse> deals = houseDealService.getDealsByAptSeq(aptSeq);
        log.info("아파트별 거래 내역 조회 완료: {} 개", deals.size());
        return ResponseEntity.ok(CommonResponse.success(deals));
    }
}

