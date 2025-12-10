package com.ssafy.home.mapper;

import com.ssafy.home.dto.AddressResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AreaMapper {
    // TODO: 33평으로 설정되어있는 Mock data를 실제 평균 가격으로 변경 필요

    @Select("""
        SELECT
            dong_code AS dongCode,
            sido_name AS sidoName,
            NULL AS gugunName,
            NULL AS dongName,
            NULL AS aptSeq,
            NULL AS aptName,
            NULL AS aptDong,
            latitude,
            longitude,
            avg_price AS avgPrice,
            NULL as primaryPyung
        FROM dongcodes
        WHERE gugun_name IS NULL
          AND latitude BETWEEN #{minLat} AND #{maxLat}
          AND longitude BETWEEN #{minLng} AND #{maxLng}
        LIMIT 100
        """)
    List<AddressResponse> findSidoByBoundingBox(@Param("minLat") Double minLat,
                                            @Param("maxLat") Double maxLat,
                                            @Param("minLng") Double minLng,
                                            @Param("maxLng") Double maxLng);

    @Select("""
        SELECT
            dong_code AS dongCode,
            sido_name AS sidoName,
            gugun_name AS gugunName,
            NULL AS dongName,
            NULL AS aptSeq,
            NULL AS aptName,
            NULL AS aptDong,
            latitude,
            longitude,
            avg_price AS avgPrice,
            NULL as primaryPyung
        FROM dongcodes
        WHERE gugun_name IS NOT NULL AND dong_name IS NULL
            AND latitude BETWEEN #{minLat} AND #{maxLat}
            AND longitude BETWEEN #{minLng} AND #{maxLng}
        LIMIT 100
        """)
    List<AddressResponse> findGugunByBoundingBox(@Param("minLat") Double minLat,
                                                @Param("maxLat") Double maxLat,
                                                @Param("minLng") Double minLng,
                                                @Param("maxLng") Double maxLng);

    @Select("""
        SELECT
            dong_code AS dongCode,
            sido_name AS sidoName,
            gugun_name AS gugunName,
            dong_name AS dongName,
            NULL AS aptSeq,
            NULL AS aptName,
            NULL AS aptDong,
            latitude,
            longitude,
            avg_price AS avgPrice,
            NULL as primaryPyung
        FROM dongcodes
        WHERE dong_name IS NOT NULL
            AND latitude BETWEEN #{minLat} AND #{maxLat}
            AND longitude BETWEEN #{minLng} AND #{maxLng}
        LIMIT 100
        """)
    List<AddressResponse> findDongByBoundingBox(@Param("minLat") Double minLat,
                                                @Param("maxLat") Double maxLat,
                                                @Param("minLng") Double minLng,
                                                @Param("maxLng") Double maxLng);

    @Select("""
        SELECT
            hi.dong_code AS dongCode,
            sido_name AS sidoName,
            gugun_name AS gugunName,
            dong_name AS dongName,
            apt_seq AS aptSeq,
            apt_nm AS aptName,
            NULL AS aptDong,
            hi.latitude,
            hi.longitude,
            hi.avg_price AS avgPrice,
            33 as primaryPyung
        FROM houseinfos hi, dongcodes dc
        WHERE hi.dong_code = dc.dong_code
            AND hi.latitude BETWEEN #{minLat} AND #{maxLat}
            AND hi.longitude BETWEEN #{minLng} AND #{maxLng}
        LIMIT 100
        """)
    List<AddressResponse> findAptByBoundingBox(@Param("minLat") Double minLat,
                                                @Param("maxLat") Double maxLat,
                                                @Param("minLng") Double minLng,
                                                @Param("maxLng") Double maxLng);

    @Select("""
        SELECT
            hi.dong_code AS dongCode,
            sido_name AS sidoName,
            gugun_name AS gugunName,
            dong_name AS dongName,
            hi.apt_seq AS aptSeq,
            apt_nm AS aptName,
            ads.apt_dong AS aptDong,
            ads.latitude,
            ads.longitude,
            ads.avg_price AS avgPrice,
            33 as primaryPyung
        FROM houseinfos hi, dongcodes dc, apt_dong_stats ads
        WHERE hi.dong_code = dc.dong_code
            AND hi.apt_seq = ads.apt_seq
            AND hi.latitude BETWEEN #{minLat} AND #{maxLat}
            AND hi.longitude BETWEEN #{minLng} AND #{maxLng}
        LIMIT 100
        """)
    List<AddressResponse> findAptDongByBoundingBox(@Param("minLat") Double minLat,
                                             @Param("maxLat") Double maxLat,
                                             @Param("minLng") Double minLng,
                                             @Param("maxLng") Double maxLng);
}

