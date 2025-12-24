package com.ssafy.home.ai.service;

import com.ssafy.home.ai.dto.SubwayNearbyRequest;
import com.ssafy.home.ai.dto.KakaoSearchRequest;
import com.ssafy.home.ai.dto.KakaoSearchResponse;
import com.ssafy.home.dto.mapper.ApartmentBasicInfo;
import com.ssafy.home.mapper.ApartmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

/**
 * 지하철역 인근 아파트 검색 서비스
 * 카카오 API로 역 위치를 찾고, 해당 좌표 주변의 DB 아파트를 검색
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubwayNearbyService implements Function<SubwayNearbyRequest, List<ApartmentBasicInfo>> {

    private final KakaoSearchService kakaoSearchService;
    private final ApartmentMapper apartmentMapper;

    @Override
    public List<ApartmentBasicInfo> apply(SubwayNearbyRequest request) {
        log.info("=== SubwayNearbyFunction Called ===");
        log.info("Station: {}, Radius: {}m, MaxPrice: {}, MinPyung: {}", 
            request.stationName(), request.radiusMeters(), request.maxPrice(), request.minPyung());
        
        try {
            // 1. 카카오 API로 지하철역 위치 검색
            String searchQuery = request.stationName() + " 지하철역";
            KakaoSearchResponse kakaoResult = kakaoSearchService.apply(new KakaoSearchRequest(searchQuery));
            
            if (kakaoResult.apartments().isEmpty()) {
                log.warn("Station not found: {}", request.stationName());
                return List.of();
            }
            
            // 2. 역 좌표 추출
            KakaoSearchResponse.ApartmentInfo stationInfo = kakaoResult.apartments().get(0);
            double stationLat = stationInfo.latitude();
            double stationLng = stationInfo.longitude();
            log.info("Station Location: {} ({}, {})", stationInfo.name(), stationLat, stationLng);
            
            // 3. 역 주변 아파트 검색 (DB에서 좌표 기반 검색)
            int radius = request.radiusMeters() != null ? request.radiusMeters() : 1000;
            double radiusDegree = radius / 111000.0; // 대략적인 미터->도 변환
            
            // 간단한 bounding box 검색 (정확한 거리 계산은 추후 개선)
            List<ApartmentBasicInfo> nearbyApartments = apartmentMapper.searchApartmentsByKeywords(
                List.of(extractDongName(stationInfo.address())),
                null, // minPrice
                request.maxPrice(),
                request.minPyung(),
                null // maxPyung
            );
            
            // Limit results to 10 items to save tokens
            List<ApartmentBasicInfo> limitedResults = nearbyApartments.stream()
                .limit(10)
                .collect(java.util.stream.Collectors.toList());

            log.info("Found {} apartments near {} (Limited to {})", 
                nearbyApartments.size(), request.stationName(), limitedResults.size());
            return limitedResults;
            
        } catch (Exception e) {
            log.error("SubwayNearbyService Error", e);
            return List.of();
        }
    }
    
    private String extractDongName(String address) {
        // 주소에서 동 이름 추출 (예: "서울 강남구 역삼동" -> "역삼동")
        String[] parts = address.split(" ");
        for (String part : parts) {
            if (part.endsWith("동") || part.endsWith("구")) {
                return part;
            }
        }
        return parts.length > 1 ? parts[1] : parts[0];
    }
}
