package com.ssafy.home.service.impl;

import com.ssafy.home.dto.HouseDealResponse;
import com.ssafy.home.repository.HouseDealRepository;
import com.ssafy.home.service.HouseDealService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HouseDealServiceImpl implements HouseDealService {

    private final HouseDealRepository houseDealRepository;

    @Override
    public List<HouseDealResponse> getRecentDeals(String dongCode, int limit) {
        return houseDealRepository.findRecentDeals(dongCode, limit);
    }

    @Override
    public List<HouseDealResponse> getDealsByAptSeq(String aptSeq) {
        return houseDealRepository.findDealsByAptSeq(aptSeq);
    }

    @Override
    @Deprecated
    public List<HouseDealResponse> getHouseDealsByBounds(double minLat, double maxLat, double minLng, double maxLng, int limit) {
        return houseDealRepository.findHouseDealsByBounds(minLat, maxLat, minLng, maxLng, limit);
    }
}

