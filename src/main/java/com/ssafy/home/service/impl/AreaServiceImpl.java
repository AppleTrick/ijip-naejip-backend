package com.ssafy.home.service.impl;

import com.ssafy.home.dto.AddressResponse;
import com.ssafy.home.dto.AreaScope;
import com.ssafy.home.repository.AreaRepository;
import com.ssafy.home.service.AreaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AreaServiceImpl implements AreaService {

    private final AreaRepository areaRepository;

    @Override
    public List<AddressResponse> searchAreaAddress(Double minLat, Double maxLat, Double minLng, Double maxLng, AreaScope scope) {
        switch (scope) {
            case APT_DONG -> {
                return areaRepository.findAptDongByBoundingBox(minLat, maxLat, minLng, maxLng);
            }
            case APT -> {
                return areaRepository.findAptByBoundingBox(minLat, maxLat, minLng, maxLng);
            }
            case DONG -> {
                return areaRepository.findDongByBoundingBox(minLat, maxLat, minLng, maxLng);
            }
            case GUGUN -> {
                return areaRepository.findGugunByBoundingBox(minLat, maxLat, minLng, maxLng);
            }
            case SIDO -> {
                return areaRepository.findSidoByBoundingBox(minLat, maxLat, minLng, maxLng);
            }
            default -> {
                log.warn("알 수 없는 범위(scope) 값: {}", scope);
                throw new IllegalArgumentException("알 수 없는 범위(scope) 값: " + scope);
            }
        }
    }
}

