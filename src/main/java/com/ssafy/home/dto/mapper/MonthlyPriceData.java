package com.ssafy.home.dto.mapper;

public record MonthlyPriceData(
    String month,  // YYYY-MM 형식
    Integer avgPrice,
    Integer transactionCount
) {
}

