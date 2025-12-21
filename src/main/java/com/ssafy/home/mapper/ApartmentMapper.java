package com.ssafy.home.mapper;

import com.ssafy.home.dto.mapper.ApartmentBasicInfo;
import com.ssafy.home.dto.mapper.MonthlyPriceData;
import com.ssafy.home.dto.mapper.TransactionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApartmentMapper {

    /**
     * 아파트 기본 정보 조회
     * @param aptSeq 아파트 고유번호
     * @return 아파트 정보
     */
    ApartmentBasicInfo findApartmentInfo(@Param("aptSeq") String aptSeq);

    /**
     * 특정 평형의 평균 가격 조회
     * @param aptSeq 아파트 고유번호
     * @param pyung 평형
     * @return 평균 가격
     */
    Integer findAvgPriceByPyung(@Param("aptSeq") String aptSeq, @Param("pyung") int pyung);

    /**
     * 아파트의 평형 타입 리스트 조회
     * @param aptSeq 아파트 고유번호
     * @return 평형 타입 리스트
     */
    List<Integer> findPyungTypesByAptSeq(@Param("aptSeq") String aptSeq);

    /**
     * 특정 평형이 존재하는지 확인
     * @param aptSeq 아파트 고유번호
     * @param pyung 평형
     * @return 존재 여부
     */
    boolean existsPyung(@Param("aptSeq") String aptSeq, @Param("pyung") int pyung);

    /**
     * 최근 거래 내역 조회 (평형 필터링 포함)
     * @param aptSeq 아파트 고유번호
     * @param pyung 평형 (null이면 전체)
     * @param limit 조회 개수
     * @return 거래 내역 리스트
     */
    List<TransactionRecord> findRecentTransactions(
        @Param("aptSeq") String aptSeq,
        @Param("pyung") Integer pyung,
        @Param("limit") int limit
    );

    /**
     * 월별 평균 가격 조회 (6개월)
     * @param aptSeq 아파트 고유번호
     * @param pyung 평형 (null이면 전체)
     * @param startMonth 시작 월 (YYYYMM)
     * @param endMonth 종료 월 (YYYYMM)
     * @return 월별 가격 리스트
     */
    List<MonthlyPriceData> findMonthlyPriceTrend(
        @Param("aptSeq") String aptSeq,
        @Param("pyung") Integer pyung,
        @Param("startMonth") String startMonth,
        @Param("endMonth") String endMonth
    );

    /**
     * 특정 날짜 이전의 가장 최근 거래 평균 가격 조회
     * @param aptSeq 아파트 고유번호
     * @param pyung 평형 (null이면 전체)
     * @param beforeMonth 기준 월 (YYYYMM) - 이 날짜 이전의 거래 조회
     * @return 평균 가격 (거래가 없으면 null)
     */
    Integer findLastAvgPriceBeforeMonth(
        @Param("aptSeq") String aptSeq,
        @Param("pyung") Integer pyung,
        @Param("beforeMonth") String beforeMonth
    );

    /**
     * 키워드 기반 아파트 검색
     * @param keywords 검색 키워드 리스트
     * @return 아파트 기본 정보 리스트
     */
    List<ApartmentBasicInfo> searchApartmentsByKeywords(@Param("keywords") List<String> keywords);
}

