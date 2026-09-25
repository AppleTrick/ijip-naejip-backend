package com.ssafy.home.ai.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * 단지 정보 카드용 조회 — 단지 기본 정보와 최근 실거래 요약
 */
@Mapper
public interface AptFactMapper {

    @Select("""
            SELECT h.apt_seq, h.apt_nm, h.build_year, h.latitude, h.longitude, h.dong_code,
                   d.sido_name, d.gugun_name, d.dong_name
            FROM houseinfos h
            LEFT JOIN dongcodes d ON h.dong_code = d.dong_code
            WHERE h.apt_seq = #{aptSeq}
            """)
    Map<String, Object> findApartment(@Param("aptSeq") String aptSeq);

    /** 단지의 기간 내 거래 수, 평균가(만원), 평당가(만원) */
    @Select("""
            SELECT COUNT(*) AS deal_count,
                   ROUND(AVG(deal_amount)) AS avg_amount,
                   ROUND(AVG(deal_amount / exclu_use_ar) * 3.3058) AS price_per_pyung
            FROM housedeals
            WHERE apt_seq = #{aptSeq} AND deal_date >= #{sinceDate}
            """)
    Map<String, Object> summarizeApartmentDeals(@Param("aptSeq") String aptSeq, @Param("sinceDate") int sinceDate);

    /** 단지의 [fromDate, untilDate) 기간 평당가(만원) — 시세 흐름 비교용 */
    @Select("""
            SELECT COUNT(*) AS deal_count,
                   ROUND(AVG(deal_amount / exclu_use_ar) * 3.3058) AS price_per_pyung
            FROM housedeals
            WHERE apt_seq = #{aptSeq} AND deal_date >= #{fromDate} AND deal_date < #{untilDate}
            """)
    Map<String, Object> summarizeApartmentDealsBetween(@Param("aptSeq") String aptSeq,
                                                       @Param("fromDate") int fromDate, @Param("untilDate") int untilDate);

    /** 구·군(법정동 코드 앞 5자리)의 기간 내 거래 수, 평당가(만원) */
    @Select("""
            SELECT COUNT(*) AS deal_count,
                   ROUND(AVG(hd.deal_amount / hd.exclu_use_ar) * 3.3058) AS price_per_pyung
            FROM housedeals hd
            JOIN houseinfos h ON hd.apt_seq = h.apt_seq
            WHERE h.dong_code LIKE CONCAT(#{gugunCode}, '%') AND hd.deal_date >= #{sinceDate}
            """)
    Map<String, Object> summarizeGugunDeals(@Param("gugunCode") String gugunCode, @Param("sinceDate") int sinceDate);
}
