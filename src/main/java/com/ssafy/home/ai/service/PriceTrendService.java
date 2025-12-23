package com.ssafy.home.ai.service;

import com.ssafy.home.ai.dto.PriceTrendRequest;
import com.ssafy.home.ai.dto.PriceTrendResponse;
import com.ssafy.home.dto.mapper.ApartmentBasicInfo;
import com.ssafy.home.dto.mapper.MonthlyPriceData;
import com.ssafy.home.mapper.ApartmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 아파트 가격 동향 조회 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriceTrendService implements Function<PriceTrendRequest, PriceTrendResponse> {

    private final ApartmentMapper apartmentMapper;

    @Override
    public PriceTrendResponse apply(PriceTrendRequest request) {
        log.info("=== PriceTrendFunction Called ===");
        log.info("AptName: {}, Region: {}", request.aptName(), request.region());
        
        try {
            // 1. 아파트 이름으로 검색
            List<ApartmentBasicInfo> apartments = apartmentMapper.searchByAptNames(List.of(request.aptName()));
            
            if (apartments.isEmpty()) {
                log.warn("Apartment not found: {}", request.aptName());
                return new PriceTrendResponse(
                    request.aptName(),
                    "정보 없음",
                    null,
                    null,
                    List.of(),
                    "조회 불가"
                );
            }
            
            ApartmentBasicInfo apt = apartments.get(0);
            log.info("Found apartment: {} - {}", apt.aptName(), apt.address());
            
            // 2. 최근 6개월 가격 동향 조회
            LocalDate now = LocalDate.now();
            String endMonth = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
            String startMonth = now.minusMonths(6).format(DateTimeFormatter.ofPattern("yyyyMM"));
            
            List<MonthlyPriceData> monthlyData = apartmentMapper.findMonthlyPriceTrend(
                apt.aptSeq(), null, startMonth, endMonth
            );
            
            // 3. 가격 동향 분석
            String trend = analyzeTrend(monthlyData);
            
            List<PriceTrendResponse.MonthlyPrice> priceHistory = monthlyData.stream()
                .map(m -> new PriceTrendResponse.MonthlyPrice(m.month(), m.avgPrice(), m.transactionCount()))
                .collect(Collectors.toList());
            
            log.info("Price trend for {}: {} ({} months data)", apt.aptName(), trend, priceHistory.size());
            
            return new PriceTrendResponse(
                apt.aptName(),
                apt.address(),
                apt.avgPrice(),
                apt.buildYear(),
                priceHistory,
                trend
            );
            
        } catch (Exception e) {
            log.error("PriceTrendService Error", e);
            return new PriceTrendResponse(
                request.aptName(),
                "오류 발생",
                null,
                null,
                List.of(),
                "조회 실패"
            );
        }
    }
    
    private String analyzeTrend(List<MonthlyPriceData> data) {
        if (data.size() < 2) {
            return "데이터 부족";
        }
        
        int firstPrice = data.get(0).avgPrice();
        int lastPrice = data.get(data.size() - 1).avgPrice();
        double changePercent = ((double)(lastPrice - firstPrice) / firstPrice) * 100;
        
        if (changePercent > 3) {
            return String.format("상승 (%.1f%%)", changePercent);
        } else if (changePercent < -3) {
            return String.format("하락 (%.1f%%)", changePercent);
        } else {
            return "보합";
        }
    }
}
