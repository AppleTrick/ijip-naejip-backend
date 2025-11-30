package com.ssafy.home.service;

import com.ssafy.home.dto.AddressResponse;
import com.ssafy.home.dto.AreaScope;
import com.ssafy.home.mapper.AreaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AreaService {
    private final AreaMapper areaMapper;

    /**
     * 특정 동코드의 최근 거래 내역 조회
     */
    public List<AddressResponse> searchAreaAddress(Double minLat, Double maxLat, Double minLng, Double maxLng, AreaScope scope) {
        log.debug("searchAreaAddress called with minLat={}, maxLat={}, minLng={}, maxLng={}, scope={}",
                minLat, maxLat, minLng, maxLng, scope);

        switch (scope) {
            case dong -> {
                return areaMapper.findDongByBoundingBox(minLat, maxLat, minLng, maxLng);
            }
            case gugun -> {
                return areaMapper.findGugunByBoundingBox(minLat, maxLat, minLng, maxLng);
            }
            case sido -> {
                return areaMapper.findSidoByBoundingBox(minLat, maxLat, minLng, maxLng);
            }
            default -> {
                log.warn("알 수 없는 범위(scope) 값: {}", scope);
                throw new IllegalArgumentException("알 수 없는 범위(scope) 값: " + scope);
            }
        }
    }
}

