package com.ssafy.home.mapper;

import com.ssafy.home.dto.AddressResponse;
import com.ssafy.home.dto.GeoBoundParam;
import com.ssafy.home.dto.PriceRangeParam;
import com.ssafy.home.dto.PyungRangeParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AreaMapper {

    // 시도 단위 조회 (XML Mapper 사용)
    List<AddressResponse> findSidoByBoundingBox(@Param("geo") GeoBoundParam geo,
                                                 @Param("price") PriceRangeParam price);

    // 구군 단위 조회 (XML Mapper 사용)
    List<AddressResponse> findGugunByBoundingBox(@Param("geo") GeoBoundParam geo,
                                                  @Param("price") PriceRangeParam price);

    // 동 단위 조회 (XML Mapper 사용)
    List<AddressResponse> findDongByBoundingBox(@Param("geo") GeoBoundParam geo,
                                                 @Param("price") PriceRangeParam price);

    // 아파트 단위 조회: 대표 평형 기준 필터링 (XML Mapper 사용)
    List<AddressResponse> findAptByBoundingBox(@Param("geo") GeoBoundParam geo,
                                                @Param("price") PriceRangeParam price,
                                                @Param("pyung") PyungRangeParam pyung);

    // 아파트 동 단위 조회: 대표 평형 기준 필터링 (XML Mapper 사용)
    List<AddressResponse> findAptDongByBoundingBox(@Param("geo") GeoBoundParam geo,
                                             @Param("price") PriceRangeParam price,
                                             @Param("pyung") PyungRangeParam pyung);
}
