package com.ssafy.home.service.impl;

import com.ssafy.home.dto.*;
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
    public List<AddressResponse> searchAreaAddress(GeoBoundParam geoBoundParam, AreaScope scope,
                                                   PriceRangeParam priceRangeParam, PyungRangeParam pyungRangeParam) {
        switch (scope) {
            case APT_DONG -> {
                return areaRepository.findAptDongByBoundingBox(geoBoundParam, priceRangeParam, pyungRangeParam);
            }
            case APT -> {
                return areaRepository.findAptByBoundingBox(geoBoundParam, priceRangeParam, pyungRangeParam);
            }
            case DONG -> {
                return areaRepository.findDongByBoundingBox(geoBoundParam, priceRangeParam);
            }
            case GUGUN -> {
                return areaRepository.findGugunByBoundingBox(geoBoundParam, priceRangeParam);
            }
            case SIDO -> {
                return areaRepository.findSidoByBoundingBox(geoBoundParam, priceRangeParam);
            }
            default -> {
                log.warn("알 수 없는 범위(scope) 값: {}", scope);
                throw new IllegalArgumentException("알 수 없는 범위(scope) 값: " + scope);
            }
        }
    }
}

