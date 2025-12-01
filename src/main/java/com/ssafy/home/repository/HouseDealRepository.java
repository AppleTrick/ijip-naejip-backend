package com.ssafy.home.repository;

import com.ssafy.home.dto.HouseDealResponse;

import java.util.List;


public interface HouseDealRepository {
    List<HouseDealResponse> findRecentDeals(String dongCode, int limit);
    List<HouseDealResponse> findDealsByAptSeq(String aptSeq);
    List<HouseDealResponse> findHouseDealsByBounds(double minLat, double maxLat, double minLng, double maxLng, int limit);
}

