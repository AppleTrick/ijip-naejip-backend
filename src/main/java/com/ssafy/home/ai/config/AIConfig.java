package com.ssafy.home.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class AIConfig {

    @Bean
    public org.springframework.web.client.RestClient.Builder restClientBuilder() {
        return org.springframework.web.client.RestClient.builder()
                .requestInterceptor((request, body, execution) -> {
                    // Cloudflare 등을 우회하기 위해 일반적인 브라우저 User-Agent 추가
                    request.getHeaders().set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

                    // 헤더는 Authorization(API 키)을 포함하므로 로그에 남기지 않는다
                    log.info(">>> [AI REQUEST] {} {} ({} bytes)", request.getMethod(), request.getURI(), body.length);
                    log.debug(">>> [AI BODY] {}", new String(body, java.nio.charset.StandardCharsets.UTF_8));

                    long start = System.currentTimeMillis();
                    var response = execution.execute(request, body);

                    log.info("<<< [AI RESPONSE STATUS] {} ({}ms)", response.getStatusCode(), System.currentTimeMillis() - start);
                    return response;
                });
    }
    @Bean
    @org.springframework.context.annotation.Description("행정구역명, 가격 범위(minPrice, maxPrice), 평수 범위(minPyung, maxPyung)를 필터로 사용하여 로컬 DB에서 아파트를 검색합니다. (가격 단위: 만원)")
    public java.util.function.Function<com.ssafy.home.ai.dto.LocalSearchRequest, java.util.List<com.ssafy.home.dto.mapper.ApartmentBasicInfo>> localSearchFunction(com.ssafy.home.ai.service.LocalSearchService localSearchService) {
        return localSearchService;
    }

    @Bean
    @org.springframework.context.annotation.Description("카카오 지도를 검색하여 아파트 및 장소 정보를 가져옵니다. (예: 서울 복층 아파트, 강남 테라스 아파트, 올림픽공원역 주변 아파트)")
    public java.util.function.Function<com.ssafy.home.ai.dto.KakaoSearchRequest, com.ssafy.home.ai.dto.KakaoSearchResponse> kakaoSearchFunction(com.ssafy.home.ai.service.KakaoSearchService kakaoSearchService) {
        return kakaoSearchService;
    }

    @Bean
    @org.springframework.context.annotation.Description("특정 지하철역 인근의 아파트를 검색합니다. stationName(역 이름), radiusMeters(반경), maxPrice(최대가격), minPyung(최소평수) 파라미터 사용.")
    public java.util.function.Function<com.ssafy.home.ai.dto.SubwayNearbyRequest, java.util.List<com.ssafy.home.dto.mapper.ApartmentBasicInfo>> subwayNearbyFunction(com.ssafy.home.ai.service.SubwayNearbyService subwayNearbyService) {
        return subwayNearbyService;
    }

    @Bean
    @org.springframework.context.annotation.Description("특정 아파트의 최근 6개월 가격 동향과 상승/하락 추세를 조회합니다. aptName(아파트명), region(지역) 파라미터 사용.")
    public java.util.function.Function<com.ssafy.home.ai.dto.PriceTrendRequest, com.ssafy.home.ai.dto.PriceTrendResponse> priceTrendFunction(com.ssafy.home.ai.service.PriceTrendService priceTrendService) {
        return priceTrendService;
    }
}
