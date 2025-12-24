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
            
            // Limit results to 10 items to save tokens
            List<ApartmentBasicInfo> limitedResults = results.stream()
                .limit(10)
                .collect(java.util.stream.Collectors.toList());

            log.info("Search Results Count: {} (Limited to {})", results.size(), limitedResults.size());
            if (!limitedResults.isEmpty()) {
                log.info("Sample Result: {} - {} (가격: {}만원)", 
                    limitedResults.get(0).aptName(), 
                    limitedResults.get(0).address(), 
                    limitedResults.get(0).avgPrice());
            }
            return limitedResults;
        } catch (Exception e) {
            log.error("Local DB Search Error", e);
            return List.of();
        }
    }
}
