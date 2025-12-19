package com.ssafy.home.service.impl;

import com.ssafy.home.dto.*;
import com.ssafy.home.dto.mapper.ApartmentBasicInfo;
import com.ssafy.home.dto.mapper.MonthlyPriceData;
import com.ssafy.home.dto.mapper.TransactionRecord;
import com.ssafy.home.mapper.ApartmentMapper;
import com.ssafy.home.service.ApartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApartmentServiceImpl implements ApartmentService {

    private final ApartmentMapper apartmentMapper;

    @Override
    public ApartmentDetailResponse getApartmentDetail(String aptSeq, String pyung) {
        log.info("아파트 상세 정보 조회 시작 - aptSeq: {}, pyung: {}", aptSeq, pyung);

        // 1. 아파트 존재 여부 확인
        ApartmentBasicInfo aptInfo = apartmentMapper.findApartmentInfo(aptSeq);
        if (aptInfo == null) {
            log.warn("아파트를 찾을 수 없습니다: {}", aptSeq);
            throw new IllegalArgumentException("아파트를 찾을 수 없습니다: " + aptSeq);
        }

        // 2. 평형 검증 및 변환
        Integer pyungInt = null;
        if (!"all".equalsIgnoreCase(pyung)) {
            try {
                pyungInt = Integer.parseInt(pyung);
                if (!apartmentMapper.existsPyung(aptSeq, pyungInt)) {
                    log.warn("해당 평형이 존재하지 않습니다 - aptSeq: {}, pyung: {}", aptSeq, pyung);
                    throw new IllegalArgumentException("해당 평형이 존재하지 않습니다: " + pyung);
                }
            } catch (NumberFormatException e) {
                log.error("평형 파라미터가 유효하지 않습니다: {}", pyung);
                throw new IllegalArgumentException("평형 파라미터가 유효하지 않습니다: " + pyung);
            }
        }

        // 3. 평형 타입 리스트 조회
        List<Integer> pyungTypes = apartmentMapper.findPyungTypesByAptSeq(aptSeq);
        List<String> pyungTypeStrings = pyungTypes.stream()
            .map(String::valueOf)
            .toList();

        // 4. 평형별 평균 가격 조회
        Integer avgPrice = aptInfo.avgPrice(); // 기본값: 전체 평균
        if (pyungInt != null) {
            // 특정 평형이 지정된 경우, 해당 평형의 평균 가격 조회
            Integer pyungAvgPrice = apartmentMapper.findAvgPriceByPyung(aptSeq, pyungInt);
            if (pyungAvgPrice != null) {
                avgPrice = pyungAvgPrice;
                log.debug("평형별 평균 가격 조회 - pyung: {}, avgPrice: {}", pyungInt, avgPrice);
            }
        }

        // 5. 아파트 기본 정보 DTO 생성
        ApartmentDetailResponse.ApartmentInfoDto infoDto = new ApartmentDetailResponse.ApartmentInfoDto(
            aptInfo.aptSeq(),
            aptInfo.aptName(),
            aptInfo.address(),
            avgPrice,
            aptInfo.buildYear(),
            pyungTypeStrings
        );

        // 6. 최근 거래 내역 조회
        List<TransactionRecord> transactions = apartmentMapper.findRecentTransactions(aptSeq, pyungInt, 10);
        List<ApartmentDetailResponse.RecentTransactionDto> transactionDtos = transactions.stream()
            .map(t -> new ApartmentDetailResponse.RecentTransactionDto(
                formatDate(t.transactionDate()),
                String.valueOf(t.pyungType()),
                t.dealAmount(),
                parseIntSafely(t.floor()),
                t.aptDong()
            ))
            .toList();

        // 7. 3년 가격 추이 계산
        ApartmentDetailResponse.PriceTrendDto priceTrend = calculatePriceTrend(aptSeq, pyungInt);

        log.info("아파트 상세 정보 조회 완료 - aptSeq: {}, 거래 내역 수: {}", aptSeq, transactionDtos.size());

        return new ApartmentDetailResponse(
            pyung,
            infoDto,
            transactionDtos,
            priceTrend
        );
    }

    /**
     * 3년 가격 추이 계산
     */
    private ApartmentDetailResponse.PriceTrendDto calculatePriceTrend(String aptSeq, Integer pyungInt) {
        LocalDate now = LocalDate.now();
        String endMonth = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String startMonth = now.minusMonths(35).format(DateTimeFormatter.ofPattern("yyyyMM"));

        log.debug("가격 추이 조회 - startMonth: {}, endMonth: {}", startMonth, endMonth);

        // 월별 가격 데이터 조회
        List<MonthlyPriceData> monthlyPrices = apartmentMapper.findMonthlyPriceTrend(
            aptSeq, pyungInt, startMonth, endMonth
        );

        // 3년치 데이터 생성 (거래 없는 월은 이전 월 데이터로 채우기)
        List<ApartmentDetailResponse.PriceTrendDto.PriceDataPointDto> dataPoints = new ArrayList<>();
        Integer lastAvgPrice = 0;

        for (int i = 35; i >= 0; i--) {
            LocalDate targetMonth = now.minusMonths(i);
            String monthStr = targetMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));

            // 해당 월의 데이터 찾기
            Optional<MonthlyPriceData> found = monthlyPrices.stream()
                .filter(mp -> mp.month().equals(monthStr))
                .findFirst();

            if (found.isPresent()) {
                lastAvgPrice = found.get().avgPrice();
                dataPoints.add(new ApartmentDetailResponse.PriceTrendDto.PriceDataPointDto(
                    monthStr,
                    lastAvgPrice,
                    found.get().transactionCount()
                ));
                log.debug("월별 데이터 - {}: {} (거래 {}건)", monthStr, lastAvgPrice, found.get().transactionCount());
            } else {
                // 거래 없는 월: 이전 달의 가격 사용 (이전 데이터가 없으면 0)
                dataPoints.add(new ApartmentDetailResponse.PriceTrendDto.PriceDataPointDto(
                    monthStr,
                    lastAvgPrice,
                    0
                ));
                log.debug("월별 데이터 - {}: {} (거래 없음, 이전 가격 사용)", monthStr, lastAvgPrice);
            }
        }

        return new ApartmentDetailResponse.PriceTrendDto("month", dataPoints);
    }

    /**
     * YYYYMMDD 형식을 YYYY-MM-DD 형식으로 변환
     */
    private String formatDate(Integer dateInt) {
        if (dateInt == null) {
            return "";
        }
        String dateStr = String.valueOf(dateInt);
        if (dateStr.length() != 8) {
            return dateStr;
        }
        return dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6) + "-" + dateStr.substring(6, 8);
    }

    /**
     * String을 Integer로 안전하게 변환
     */
    private Integer parseIntSafely(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("정수 변환 실패: {}", value);
            return 0;
        }
    }
}

