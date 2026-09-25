package com.ssafy.home.ai.neighborhood;

import com.ssafy.home.ai.mapper.AptFactMapper;
import com.ssafy.home.ai.neighborhood.ApartmentFacts.DealSummary;
import com.ssafy.home.ai.neighborhood.ApartmentFacts.Nearby;
import com.ssafy.home.ai.neighborhood.ApartmentFacts.Spot;
import com.ssafy.home.ai.neighborhood.KakaoLocalClient.Place;
import com.ssafy.home.ai.neighborhood.KakaoLocalClient.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * 단지 정보 카드 생성 — 실거래 DB(최근 12개월 시세)와 카카오 로컬(주변 시설)을 모은다.
 * 주변 시설은 자주 바뀌지 않으므로 단지별로 7일간 메모리에 캐시한다.
 */
@Slf4j
@Service
public class ApartmentFactService implements DisposableBean {

    private static final Duration CACHE_TTL = Duration.ofDays(7);
    private static final int CACHE_MAX = 1000;
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final AptFactMapper aptFactMapper;
    private final KakaoLocalClient kakao;
    private final ExecutorService executor = Executors.newFixedThreadPool(6);

    private record CacheEntry(ApartmentFacts facts, Instant createdAt) {}

    private final Map<String, CacheEntry> cache = Collections.synchronizedMap(new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
            return size() > CACHE_MAX;
        }
    });

    public ApartmentFactService(AptFactMapper aptFactMapper, KakaoLocalClient kakao) {
        this.aptFactMapper = aptFactMapper;
        this.kakao = kakao;
    }

    /**
     * @throws IllegalArgumentException 단지가 없거나 좌표가 없을 때
     */
    public ApartmentFacts getFacts(String aptSeq) {
        CacheEntry cached = cache.get(aptSeq);
        if (cached != null && cached.createdAt().plus(CACHE_TTL).isAfter(Instant.now())) {
            return cached.facts();
        }
        ApartmentFacts facts = collect(aptSeq);
        cache.put(aptSeq, new CacheEntry(facts, Instant.now()));
        return facts;
    }

    private ApartmentFacts collect(String aptSeq) {
        Map<String, Object> apt = aptFactMapper.findApartment(aptSeq);
        if (apt == null || apt.get("latitude") == null || apt.get("longitude") == null) {
            throw new IllegalArgumentException("단지를 찾을 수 없거나 좌표가 없습니다: " + aptSeq);
        }
        double lat = ((Number) apt.get("latitude")).doubleValue();
        double lng = ((Number) apt.get("longitude")).doubleValue();
        String region = String.join(" ", nonNull(apt.get("sido_name")), nonNull(apt.get("gugun_name")), nonNull(apt.get("dong_name"))).trim();

        LocalDate today = LocalDate.now(SEOUL);
        LocalDate from = today.minusYears(1);
        int sinceDate = Integer.parseInt(from.format(DateTimeFormatter.BASIC_ISO_DATE));
        Map<String, Object> aptDeals = aptFactMapper.summarizeApartmentDeals(aptSeq, sinceDate);
        int prevSinceDate = Integer.parseInt(from.minusYears(1).format(DateTimeFormatter.BASIC_ISO_DATE));
        Map<String, Object> prevDeals = aptFactMapper.summarizeApartmentDealsBetween(aptSeq, prevSinceDate, sinceDate);
        String dongCode = nonNull(apt.get("dong_code"));
        Map<String, Object> gugunDeals = dongCode.length() >= 5
                ? aptFactMapper.summarizeGugunDeals(dongCode.substring(0, 5), sinceDate)
                : null;
        DealSummary deals = new DealSummary(from, today,
                toLong(aptDeals, "deal_count", 0L), toLong(aptDeals, "avg_amount", null), toLong(aptDeals, "price_per_pyung", null),
                nonNull(apt.get("gugun_name")), toLong(gugunDeals, "deal_count", 0L), toLong(gugunDeals, "price_per_pyung", null),
                toLong(prevDeals, "deal_count", 0L), toLong(prevDeals, "price_per_pyung", null));

        return new ApartmentFacts(aptSeq, nonNull(apt.get("apt_nm")), region,
                apt.get("build_year") == null ? null : ((Number) apt.get("build_year")).intValue(),
                lat, lng, deals, collectNearby(lat, lng), today);
    }

    private Nearby collectNearby(double lat, double lng) {
        if (!kakao.isEnabled()) {
            return Nearby.unavailable();
        }
        int r = ApartmentFacts.RADIUS_METERS;
        var stations = async(() -> kakao.searchCategory("SW8", lng, lat, r));
        var schools = async(() -> kakao.searchCategory("SC4", lng, lat, r));
        var marts = async(() -> kakao.searchCategory("MT1", lng, lat, r));
        var hospitals = async(() -> kakao.searchCategory("HP8", lng, lat, r));
        var academies = async(() -> kakao.searchCategory("AC5", lng, lat, r));
        var kindergartens = async(() -> kakao.searchCategory("PS3", lng, lat, r));
        var parks = async(() -> kakao.searchKeyword("공원", lng, lat, r));
        try {
            SearchResult schoolResult = schools.join();
            return new Nearby(true,
                    groupStations(stations.join().places()),
                    schoolsOf(schoolResult.places(), "초등학교"),
                    schoolsOf(schoolResult.places(), "중학교"),
                    schoolsOf(schoolResult.places(), "고등학교"),
                    toSpots(marts.join().places()), marts.join().totalCount(),
                    hospitals.join().totalCount(), academies.join().totalCount(), kindergartens.join().totalCount(),
                    parksOf(parks.join().places()));
        } catch (Exception e) {
            log.warn("주변 시설 조회 실패: {}", e.getMessage());
            return Nearby.unavailable();
        }
    }

    private CompletableFuture<SearchResult> async(Supplier<SearchResult> call) {
        return CompletableFuture.supplyAsync(call, executor);
    }

    /** "공덕역 6호선", "공덕역 공항철도"처럼 노선별로 나오는 역을 하나로 묶는다 */
    static List<Spot> groupStations(List<Place> places) {
        Map<String, Spot> byName = new LinkedHashMap<>();
        for (Place p : places) {
            String[] parts = p.name().split(" ", 2);
            String station = parts[0];
            String line = parts.length > 1 ? parts[1] : "";
            byName.merge(station, new Spot(station, line, p.distance()), (a, b) -> new Spot(
                    a.name(),
                    a.detail().isEmpty() ? b.detail() : b.detail().isEmpty() || a.detail().contains(b.detail()) ? a.detail() : a.detail() + "·" + b.detail(),
                    Math.min(a.distance(), b.distance())));
        }
        return byName.values().stream().sorted(Comparator.comparingInt(Spot::distance)).toList();
    }

    static List<Spot> schoolsOf(List<Place> places, String kind) {
        return places.stream().filter(p -> p.name().endsWith(kind)).map(p -> new Spot(p.name(), "", p.distance())).toList();
    }

    /** 키워드 "공원" 결과에는 공원 화장실·주차장도 섞이므로 카테고리가 공원인 것만 남긴다 */
    static List<Spot> parksOf(List<Place> places) {
        return places.stream()
                .filter(p -> Arrays.asList(p.categoryName().split(" > ")).contains("공원"))
                .map(p -> new Spot(p.name(), "", p.distance()))
                .toList();
    }

    private static List<Spot> toSpots(List<Place> places) {
        return places.stream().map(p -> new Spot(p.name(), "", p.distance())).toList();
    }

    private static String nonNull(Object value) {
        return value == null ? "" : value.toString();
    }

    private static Long toLong(Map<String, Object> row, String key, Long defaultValue) {
        if (row == null || row.get(key) == null) {
            return defaultValue;
        }
        return ((Number) row.get(key)).longValue();
    }

    @Override
    public void destroy() {
        executor.shutdownNow();
    }
}
