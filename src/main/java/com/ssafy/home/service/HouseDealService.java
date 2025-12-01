package com.ssafy.home.service;

import com.ssafy.home.dto.HouseDealResponse;

import java.util.List;

public interface HouseDealService {
    List<HouseDealResponse> getRecentDeals(String dongCode, int limit);
    List<HouseDealResponse> getDealsByAptSeq(String aptSeq);
    @Deprecated
    List<HouseDealResponse> getHouseDealsByBounds(double minLat, double maxLat, double minLng, double maxLng, int limit);
}