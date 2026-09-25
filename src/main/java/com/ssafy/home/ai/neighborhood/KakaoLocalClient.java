package com.ssafy.home.ai.neighborhood;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

/**
 * 카카오 로컬 API — 좌표 기준 반경 내 장소 검색 (거리순)
 * https://developers.kakao.com/docs/latest/ko/local/dev-guide
 */
@Slf4j
@Component
public class KakaoLocalClient {

    public record Place(String name, String categoryName, int distance) {}

    public record SearchResult(int totalCount, List<Place> places) {
        static final SearchResult EMPTY = new SearchResult(0, List.of());
    }

    private final RestClient restClient;
    private final boolean enabled;

    public KakaoLocalClient(@Value("${kakao.rest-api-key:}") String restApiKey) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(3000);
        this.restClient = RestClient.builder()
                .baseUrl("https://dapi.kakao.com/v2/local/search")
                .defaultHeader("Authorization", "KakaoAK " + restApiKey)
                .requestFactory(requestFactory)
                .build();
        this.enabled = !restApiKey.isBlank();
        if (!enabled) {
            log.warn("kakao.rest-api-key 미설정 — 주변 시설 정보를 조회하지 않습니다.");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** 카테고리 그룹 코드(SW8 지하철역, SC4 학교 등)로 검색 */
    public SearchResult searchCategory(String categoryGroupCode, double lng, double lat, int radius) {
        return search("/category.json", "category_group_code", categoryGroupCode, lng, lat, radius);
    }

    /** 키워드로 검색 (공원처럼 카테고리 코드가 없는 장소) */
    public SearchResult searchKeyword(String query, double lng, double lat, int radius) {
        return search("/keyword.json", "query", query, lng, lat, radius);
    }

    private SearchResult search(String path, String paramName, String paramValue, double lng, double lat, int radius) {
        if (!enabled) {
            return SearchResult.EMPTY;
        }
        JsonNode body = restClient.get()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .queryParam(paramName, paramValue)
                        .queryParam("x", lng)
                        .queryParam("y", lat)
                        .queryParam("radius", radius)
                        .queryParam("sort", "distance")
                        .queryParam("size", 15)
                        .build())
                .retrieve()
                .body(JsonNode.class);
        return parse(body);
    }

    static SearchResult parse(JsonNode body) {
        if (body == null || !body.has("documents")) {
            return SearchResult.EMPTY;
        }
        List<Place> places = new ArrayList<>();
        for (JsonNode doc : body.get("documents")) {
            places.add(new Place(
                    doc.path("place_name").asText(),
                    doc.path("category_name").asText(),
                    doc.path("distance").asInt(Integer.MAX_VALUE)));
        }
        return new SearchResult(body.path("meta").path("total_count").asInt(places.size()), places);
    }
}
