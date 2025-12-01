package com.ssafy.home.repository.impl;

import com.ssafy.home.dto.HouseDealResponse;
import com.ssafy.home.mapper.HouseDealMapper;
import com.ssafy.home.repository.HouseDealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class HouseDealRepositoryImpl implements HouseDealRepository {

    private final HouseDealMapper houseDealMapper;

    @Override
    public List<HouseDealResponse> findRecentDeals(String dongCode, int limit) {
        return houseDealMapper.selectRecentDeals(dongCode, limit);
    }

    @Override
    public List<HouseDealResponse> findDealsByAptSeq(String aptSeq) {
        return houseDealMapper.selectDealsByAptSeq(aptSeq);
    }

    @Override
    @Deprecated
    public List<HouseDealResponse> findHouseDealsByBounds(double minLat, double maxLat, double minLng, double maxLng, int limit) {
        return houseDealMapper.selectHouseDealsByBounds(minLat, maxLat, minLng, maxLng, limit);
    }
}

