package com.ssafy.home.dto.mapper;

public record TransactionRecord(
    Integer transactionDate,  // YYYYMMDD 형식
    Integer pyungType,
    Integer dealAmount,
    String floor,
    String aptDong
) {
}

