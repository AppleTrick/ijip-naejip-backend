package com.ssafy.home.mapper;

import com.ssafy.home.dto.AddressResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AreaMapper {

    @Select("""
        SELECT
            dong_code AS dongCode,
            sido_name AS sidoName,
            latitude,
            longitude
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
            latitude,
            longitude
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
            latitude,
            longitude
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
}

