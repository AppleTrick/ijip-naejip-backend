package com.ssafy.home.ai.service;

import com.ssafy.home.ai.dto.KakaoSearchRequest;
import com.ssafy.home.ai.dto.KakaoSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class KakaoSearchService implements Function<KakaoSearchRequest, KakaoSearchResponse> {

    @Value("${kakao.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public KakaoSearchResponse apply(KakaoSearchRequest request) {
        log.info("Searching Kakao for: {}", request.query());
        
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Kakao API Key is missing. Returning empty results.");
            return new KakaoSearchResponse(List.of());
        }

        String encodedQuery;
        try {
            encodedQuery = java.net.URLEncoder.encode(request.query(), java.nio.charset.StandardCharsets.UTF_8.toString());
        } catch (java.io.UnsupportedEncodingException e) {
            log.error("Query encoding failed", e);
            encodedQuery = request.query();
        }
        String url = "https://dapi.kakao.com/v2/local/search/keyword.json?query=" + encodedQuery;
        
        log.info("Kakao API Request URL: {}", url);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + apiKey);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            var response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, Map.class);
            log.info("Kakao API Response Status: {}", response.getStatusCode());
            var body = response.getBody();
            if (body == null) {
                log.warn("Kakao API Response body is null");
                return new KakaoSearchResponse(List.of());
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> documents = (List<Map<String, Object>>) body.get("documents");
            log.info("Kakao API Found {} documents", documents != null ? documents.size() : 0);
            
            List<KakaoSearchResponse.ApartmentInfo> apartments = new ArrayList<>();
            if (documents != null) {
                for (var doc : documents) {
                    String placeName = (String) doc.get("place_name");
                    String addressName = (String) doc.get("address_name");
                    String roadAddressName = (String) doc.get("road_address_name");
                    String x = (String) doc.get("x");
                    String y = (String) doc.get("y");
                    double lat = Double.parseDouble(y);
                    double lng = Double.parseDouble(x);
                    
                    log.info("Extracted apartment: name={}, address={}, lat={}, lng={}", placeName, addressName, lat, lng);
                    
                    String description = String.format("주소: %s, 좌표: (%f, %f)", 
                        roadAddressName != null && !roadAddressName.isEmpty() ? roadAddressName : addressName, lat, lng);
                    
                    apartments.add(new KakaoSearchResponse.ApartmentInfo(placeName, addressName, description, lat, lng));
                }
            }
            return new KakaoSearchResponse(apartments);
        } catch (Exception e) {
            log.error("Kakao Search Error", e);
            return new KakaoSearchResponse(List.of());
        }
    }
}
