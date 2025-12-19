package com.ssafy.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "아파트 상세 정보 데이터")
public record ApartmentDetailResponse(
    @Schema(description = "선택된 평형 (all 또는 특정 평형 번호)", example = "all")
    String selectedPyung,

    @Schema(description = "아파트 기본 정보")
    ApartmentInfoDto apartmentInfo,

    @Schema(description = "최근 거래 내역 리스트")
    List<RecentTransactionDto> recentTransactions,

    @Schema(description = "6개월간 가격 변동 추이")
    PriceTrendDto priceTrend
) {
    @Schema(description = "아파트 기본 정보")
    public record ApartmentInfoDto (
        @Schema(description = "아파트 고유번호", example = "12345")
        String aptSeq,

        @Schema(description = "아파트 명", example = "잠실 엘스 아파트")
        String aptName,

        @Schema(description = "주소", example = "서울특별시 송파구 올림픽로 99")
        String address,

        @Schema(description = "전체 평형 기준 평균 가격 (만 단위)", example = "245000")
        Integer avgPrice,

        @Schema(description = "건축년도", example = "2006")
        Integer buildYear,

        @Schema(description = "아파트가 보유한 전체 평형 목록 (예: '22', '33')", example = "[\"22\", \"33\"]")
        List<String> pyungTypes
    ) {}

    @Schema(description = "최근 거래 내역 상세 (Record)")
    public record RecentTransactionDto(
        @Schema(description = "거래 날짜 (YYYY-MM-DD)", example = "2025-05-15")
        String transactionDate,

        @Schema(description = "거래된 평형 타입", example = "33")
        String pyungType,

        @Schema(description = "거래액 (만 단위)", example = "245000")
        Integer dealAmount,

        @Schema(description = "층수", example = "15")
        Integer floor,

        @Schema(description = "동 정보 (또는 동호수 정보)", example = "101")
        String aptDong
    ) {}
    @Schema(description = "가격 변동 추이 데이터")
    public record PriceTrendDto(
        @Schema(description = "기간 단위 (month만 가능)", example = "month")
        String unit,

        @Schema(description = "월별 가격 변동 데이터 포인트 리스트")
        List<PriceDataPointDto> dataPoints
    ) {
        @Schema(description = "월별 가격 데이터 포인트")
        public record PriceDataPointDto(
            @Schema(description = "해당 월 (YYYY-MM)", example = "2025-05")
            String month,

            @Schema(description = "해당 월의 평균 가격 (만 단위)", example = "245000")
            Integer avgPrice,

            @Schema(description = "해당 월의 거래량", example = "5")
            Integer transactionCount
        ) {}

    }

}
