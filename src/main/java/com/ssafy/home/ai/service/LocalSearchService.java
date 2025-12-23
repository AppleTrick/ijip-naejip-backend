package com.ssafy.home.ai.service;

import com.ssafy.home.ai.dto.LocalSearchRequest;
import com.ssafy.home.dto.mapper.ApartmentBasicInfo;
import com.ssafy.home.mapper.ApartmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalSearchService implements Function<LocalSearchRequest, List<ApartmentBasicInfo>> {

    private final ApartmentMapper apartmentMapper;

    @Override
    public List<ApartmentBasicInfo> apply(LocalSearchRequest request) {
        log.info("=== LocalSearchFunction Called ===");
        log.info("Keywords: {}", request.keywords());
        log.info("Price Range: {} ~ {} (만원)", request.minPrice(), request.maxPrice());
        log.info("Pyung Range: {} ~ {} 평", request.minPyung(), request.maxPyung());
        try {
            List<ApartmentBasicInfo> results = apartmentMapper.searchApartmentsByKeywords(
                request.keywords(),
                request.minPrice(),
                request.maxPrice(),
                request.minPyung(),
                request.maxPyung()
            );
            log.info("Search Results Count: {}", results.size());
            if (!results.isEmpty()) {
                log.info("Sample Result: {} - {} (가격: {}만원)", 
                    results.get(0).aptName(), 
                    results.get(0).address(), 
                    results.get(0).avgPrice());
            }
            return results;
        } catch (Exception e) {
            log.error("Local DB Search Error", e);
            return List.of();
        }
    }
}
