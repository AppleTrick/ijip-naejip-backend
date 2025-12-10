package com.ssafy.home.controller;

import com.ssafy.home.dto.ApartmentDetailResponse;
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

    @Operation(summary = "아파트별 상세 정보 조회", description = "특정 아파트의 상세 정보를 조회합니다")
    @GetMapping("/apartments/{aptSeq}")
    public ResponseEntity<CommonResponse<ApartmentDetailResponse>> getApartmentDetail(
            @Parameter(description = "아파트 시퀀스 (예: 11680-1)", required = true)
            @PathVariable("aptSeq") String aptSeq) {
        log.info("아파트별 상세 정보 조회 요청 - aptSeq: {}", aptSeq);
        // TODO: Mock 데이터 대신 실제 구현 필요
        // Mock data for demonstration purposes
        ApartmentDetailResponse.ApartmentInfoDto info = new ApartmentDetailResponse.ApartmentInfoDto(
                11680L,
                "잠실 엘스 아파트",
                "서울특별시 송파구 올림픽로 99",
                245000,
                2006,
                List.of("22", "33", "45")
        );

        List<ApartmentDetailResponse.RecentTransactionDto> recentTransactions = List.of(
                new ApartmentDetailResponse.RecentTransactionDto("2025-10-01", "33", 245000, 15, "101"),
                new ApartmentDetailResponse.RecentTransactionDto("2025-09-12", "22", 210000, 7, "102"),
                new ApartmentDetailResponse.RecentTransactionDto("2025-08-03", "45", 360000, 20, "103")
        );

        ApartmentDetailResponse.PriceTrendDto.PriceDataPointDto p1 = new ApartmentDetailResponse.PriceTrendDto.PriceDataPointDto("2025-05", 240000, 4);
        ApartmentDetailResponse.PriceTrendDto.PriceDataPointDto p2 = new ApartmentDetailResponse.PriceTrendDto.PriceDataPointDto("2025-06", 242000, 3);
        ApartmentDetailResponse.PriceTrendDto.PriceDataPointDto p3 = new ApartmentDetailResponse.PriceTrendDto.PriceDataPointDto("2025-07", 245000, 5);
        ApartmentDetailResponse.PriceTrendDto.PriceDataPointDto p4 = new ApartmentDetailResponse.PriceTrendDto.PriceDataPointDto("2025-08", 248000, 6);
        ApartmentDetailResponse.PriceTrendDto.PriceDataPointDto p5 = new ApartmentDetailResponse.PriceTrendDto.PriceDataPointDto("2025-09", 250000, 2);
        ApartmentDetailResponse.PriceTrendDto.PriceDataPointDto p6 = new ApartmentDetailResponse.PriceTrendDto.PriceDataPointDto("2025-10", 245000, 3);

        ApartmentDetailResponse.PriceTrendDto priceTrend = new ApartmentDetailResponse.PriceTrendDto(
                "month",
                List.of(p1, p2, p3, p4, p5, p6)
        );

        ApartmentDetailResponse apartmentDetail = new ApartmentDetailResponse(
                "all",
                info,
                recentTransactions,
                priceTrend
        );
        log.info("아파트별 상세 정보 조회 완료 - aptSeq: {}", aptSeq);
        return ResponseEntity.ok(CommonResponse.success(apartmentDetail));
    }

}
