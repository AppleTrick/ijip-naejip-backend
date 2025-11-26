package com.ssafy.home.service;

import com.ssafy.home.dto.HouseDealDto;
import com.ssafy.home.mapper.HouseDealMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HouseDealService {

    private final HouseDealMapper houseDealMapper;

    /**
     * 특정 동코드의 최근 거래 내역 조회
     */
    public List<HouseDealDto> getRecentDealsByDongCode(String dongCode, int limit) {
        return houseDealMapper.selectRecentDealsByDongCode(dongCode, limit);
    }

    /**
     * 특정 아파트의 모든 거래 내역 조회
     */
    public List<HouseDealDto> getDealsByAptSeq(String aptSeq) {
        return houseDealMapper.selectDealsByAptSeq(aptSeq);
    }

    /**
     * 특정 거래 내역 조회
     */
    public HouseDealDto getDealByNo(Integer no) {
        return houseDealMapper.selectDealByNo(no);
    }

    /**
     * 지도 영역(Bounds) 내의 거래 내역 조회
     */
    public List<HouseDealDto> getHouseDealsByBounds(double minLat, double maxLat, double minLng, double maxLng, int limit) {
        return houseDealMapper.selectHouseDealsByBounds(minLat, maxLat, minLng, maxLng, limit);
    }
}

