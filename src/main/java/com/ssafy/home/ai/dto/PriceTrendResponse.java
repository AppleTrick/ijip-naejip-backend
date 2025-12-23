package com.ssafy.home.ai.dto;

import java.util.List;

/**
 * 가격 동향 응답 DTO
 */
public record PriceTrendResponse(
    String aptName,
    String address,
    Integer currentAvgPrice,
    Integer buildYear,
    List<MonthlyPrice> priceHistory,
    String trend // "상승", "하락", "보합"
) {
    public record MonthlyPrice(
        String month,
        Integer avgPrice,
        Integer transactionCount
    ) {}
}
