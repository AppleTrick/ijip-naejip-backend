package com.ssafy.home.mapper;

import com.ssafy.home.dto.HouseDealResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HouseDealMapper {
    List<HouseDealResponse> selectRecentDeals(@Param("dongCode") String dongCode,
                                                        @Param("limit") int limit);

    List<HouseDealResponse> selectDealsByAptSeq(@Param("aptSeq") String aptSeq);

    @Deprecated
    List<HouseDealResponse> selectHouseDealsByBounds(@Param("minLat") double minLat,
                                                     @Param("maxLat") double maxLat,
                                                     @Param("minLng") double minLng,
                                                     @Param("maxLng") double maxLng,
                                                     @Param("limit") int limit);
}

