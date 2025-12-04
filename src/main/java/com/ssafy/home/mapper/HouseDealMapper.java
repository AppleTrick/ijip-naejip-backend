package com.ssafy.home.mapper;

import com.ssafy.home.dto.HouseDealResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HouseDealMapper {

    @Select("""
            SELECT
                hd.no,
                hd.apt_seq,
                hd.apt_dong,
                hd.floor,
                hd.deal_date,
                hd.exclu_use_ar,
                hd.deal_amount,
                hi.apt_nm,
                hi.road_nm,
                hi.jibun,
                hi.build_year,
                hi.latitude,
                hi.longitude,
                dc.dong_code,
                dc.sido_name,
                dc.gugun_name,
                dc.dong_name
            FROM housedeals hd
            INNER JOIN houseinfos hi ON hd.apt_seq = hi.apt_seq
            LEFT JOIN dongcodes dc ON CONCAT(hi.sgg_cd, hi.umd_cd) = dc.dong_code
            WHERE (#{dongCode} IS NULL OR #{dongCode} = '' OR
                   (dc.dong_code = #{dongCode}
                    AND hi.sgg_cd = SUBSTRING(#{dongCode}, 1, 5)
                    AND hi.umd_cd = SUBSTRING(#{dongCode}, 6, 5)))
            ORDER BY hd.deal_date DESC
            LIMIT #{limit}
        """)
    @ResultMap("houseDealResultMap")
    List<HouseDealResponse> selectRecentDeals(@Param("dongCode") String dongCode,
                                              @Param("limit") int limit);

    @Select("""
            SELECT
                hd.no,
                hd.apt_seq,
                hd.apt_dong,
                hd.floor,
                hd.deal_date,
                hd.exclu_use_ar,
                hd.deal_amount,
                hi.apt_nm,
                hi.road_nm,
                hi.jibun,
                hi.build_year,
                hi.latitude,
                hi.longitude,
                dc.dong_code,
                dc.sido_name,
                dc.gugun_name,
                dc.dong_name
            FROM housedeals hd
            INNER JOIN houseinfos hi ON hd.apt_seq = hi.apt_seq
            LEFT JOIN dongcodes dc ON CONCAT(hi.sgg_cd, hi.umd_cd) = dc.dong_code
            WHERE hd.apt_seq = #{aptSeq}
            ORDER BY hd.deal_date DESC
        """)
    @ResultMap("houseDealResultMap")
    List<HouseDealResponse> selectDealsByAptSeq(@Param("aptSeq") String aptSeq);

    @Deprecated
    @Select("""
            SELECT
                hd.no,
                hd.apt_seq,
                hd.apt_dong,
                hd.floor,
                hd.deal_date,
                hd.exclu_use_ar,
                hd.deal_amount,
                hi.apt_nm,
                hi.road_nm,
                hi.jibun,
                hi.build_year,
                hi.latitude,
                hi.longitude,
                dc.dong_code,
                dc.sido_name,
                dc.gugun_name,
                dc.dong_name
            FROM housedeals hd
            INNER JOIN houseinfos hi ON hd.apt_seq = hi.apt_seq
            LEFT JOIN dongcodes dc ON CONCAT(hi.sgg_cd, hi.umd_cd) = dc.dong_code
            WHERE hi.latitude BETWEEN #{minLat} AND #{maxLat}
              AND hi.longitude BETWEEN #{minLng} AND #{maxLng}
            ORDER BY hd.deal_date DESC
            LIMIT #{limit}
        """)
    @ResultMap("houseDealResultMap")
    List<HouseDealResponse> selectHouseDealsByBounds(@Param("minLat") double minLat,
                                                     @Param("maxLat") double maxLat,
                                                     @Param("minLng") double minLng,
                                                     @Param("maxLng") double maxLng,
                                                     @Param("limit") int limit);
}

