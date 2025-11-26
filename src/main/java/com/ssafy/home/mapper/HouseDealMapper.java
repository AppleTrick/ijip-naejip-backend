package com.ssafy.home.mapper;

import com.ssafy.home.dto.HouseDealDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HouseDealMapper {

    /**
     * 특정 동코드의 최근 거래 내역 조회
     * @param dongCode 동코드
     * @param limit 조회할 최대 개수
     * @return 최근 거래 내역 목록
     */
    List<HouseDealDto> selectRecentDealsByDongCode(@Param("dongCode") String dongCode,
                                                     @Param("limit") int limit);

    /**
     * 특정 아파트의 모든 거래 내역 조회
     * @param aptSeq 아파트 시퀀스
     * @return 거래 내역 목록
     */
    List<HouseDealDto> selectDealsByAptSeq(@Param("aptSeq") String aptSeq);

    /**
     * 특정 거래 내역 조회
     * @param no 거래 번호
     * @return 거래 내역
     */
    HouseDealDto selectDealByNo(@Param("no") Integer no);

    /**
     * 지도 영역(Bounds) 내의 거래 내역 조회
     * @param minLat 최소 위도
     * @param maxLat 최대 위도
     * @param minLng 최소 경도
     * @param maxLng 최대 경도
     * @param limit 조회할 최대 개수
     * @return 거래 내역 목록
     */
    List<HouseDealDto> selectHouseDealsByBounds(@Param("minLat") double minLat,
                                                @Param("maxLat") double maxLat,
                                                @Param("minLng") double minLng,
                                                @Param("maxLng") double maxLng,
                                                @Param("limit") int limit);
}

