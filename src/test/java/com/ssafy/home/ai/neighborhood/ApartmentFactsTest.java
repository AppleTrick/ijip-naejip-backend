package com.ssafy.home.ai.neighborhood;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.home.ai.neighborhood.ApartmentFacts.DealSummary;
import com.ssafy.home.ai.neighborhood.ApartmentFacts.Nearby;
import com.ssafy.home.ai.neighborhood.ApartmentFacts.Spot;
import com.ssafy.home.ai.neighborhood.KakaoLocalClient.Place;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** 2026-09-25 한화오벨리스크(11440-177) 실제 조회값 기반 */
class ApartmentFactsTest {

    @Test
    void 노선별로_나오는_역을_하나로_묶고_가까운_순으로_정렬한다() {
        List<Spot> stations = ApartmentFactService.groupStations(List.of(
                new Place("마포역 5호선", "", 60),
                new Place("공덕역 공항철도", "", 683),
                new Place("공덕역 6호선", "", 692),
                new Place("공덕역 5호선", "", 700)));

        assertThat(stations).extracting(Spot::name).containsExactly("마포역", "공덕역");
        assertThat(stations.get(1).detail()).isEqualTo("공항철도·6호선·5호선");
        assertThat(stations.get(1).distance()).isEqualTo(683);
    }

    @Test
    void 공원_검색에서_화장실_같은_부속시설은_뺀다() {
        List<Spot> parks = ApartmentFactService.parksOf(List.of(
                new Place("삼개어린이공원", "여행 > 공원 > 도시근린공원 > 어린이공원", 77),
                new Place("도화소어린이공원 개방화장실", "사회,공공기관 > 공공시설물 > 화장실", 116)));

        assertThat(parks).extracting(Spot::name).containsExactly("삼개어린이공원");
    }

    @Test
    void 학교를_초중고로_나눈다() {
        List<Place> schools = List.of(new Place("서울염리초등학교", "", 307), new Place("숭문중학교", "", 500), new Place("서강대학교", "", 900));

        assertThat(ApartmentFactService.schoolsOf(schools, "초등학교")).extracting(Spot::name).containsExactly("서울염리초등학교");
        assertThat(ApartmentFactService.schoolsOf(schools, "고등학교")).isEmpty();
    }

    @Test
    void 카드에_시세_비교와_없는_데이터를_명시한다() {
        String card = sampleFacts().toCardText();

        assertThat(card).contains("거래 70건", "평균 8.45억원", "평당 5,075만원", "마포구 평당 6,570만원 대비 23% 낮음");
        assertThat(card).contains("마포역(5호선) 60m", "직선거리", "배정 학교는 아님");
        assertThat(card).contains("[없는 데이터]");
    }

    @Test
    void 카카오_응답에서_이름_카테고리_거리_전체개수를_읽는다() throws Exception {
        String json = """
                {"documents":[{"place_name":"마포역 5호선","category_name":"교통,수송 > 지하철,전철 > 수도권5호선","distance":"60"}],
                 "meta":{"total_count":6}}
                """;
        KakaoLocalClient.SearchResult result = KakaoLocalClient.parse(new ObjectMapper().readTree(json));

        assertThat(result.totalCount()).isEqualTo(6);
        assertThat(result.places()).containsExactly(new Place("마포역 5호선", "교통,수송 > 지하철,전철 > 수도권5호선", 60));
    }

    @Test
    void 근거에_없는_역과_학교를_찾아낸다() {
        Set<String> known = sampleFacts().placeNames();

        assertThat(PlaceNameChecker.findUnknownPlaces("마포역에서 직선 60m, 염리초등학교가 가까워요.", known)).isEmpty();
        assertThat(PlaceNameChecker.findUnknownPlaces("개포역(3호선)이 도보 5분, 개포초등학교가 옆에 있어요.", known))
                .containsExactly("개포역", "개포초등학교");
        assertThat(PlaceNameChecker.findUnknownPlaces("이 지역은 주거지역이고 역세권이에요.", known)).isEmpty();
        assertThat(PlaceNameChecker.findUnknownPlaces("가까운 지하철역은 마포역이고, 초등학교는 염리초등학교예요.", known)).isEmpty();
    }

    static ApartmentFacts sampleFacts() {
        DealSummary deals = new DealSummary(LocalDate.of(2025, 9, 25), LocalDate.of(2026, 9, 25),
                70, 84456L, 5075L, "마포구", 2014, 6570L);
        Nearby nearby = new Nearby(true,
                List.of(new Spot("마포역", "5호선", 60), new Spot("공덕역", "공항철도·6호선", 683)),
                List.of(new Spot("서울염리초등학교", "", 307)), List.of(), List.of(),
                List.of(new Spot("홈플러스익스프레스 용강점", "", 380)), 4, 173, 30, 12,
                List.of(new Spot("삼개어린이공원", "", 77)));
        return new ApartmentFacts("11440-177", "한화오벨리스크", "서울특별시 마포구 도화동", 2004,
                37.54, 126.945, deals, nearby, LocalDate.of(2026, 9, 25));
    }
}
