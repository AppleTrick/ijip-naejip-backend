package com.ssafy.home.ai.neighborhood;

import com.ssafy.home.ai.neighborhood.ApartmentFacts.Nearby;
import com.ssafy.home.ai.neighborhood.ApartmentFacts.Spot;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 비교 레이더 차트 점수(0~10) — 단지 정보 카드의 측정값으로 계산한다. LLM은 점수를 만들지 않는다.
 * 같은 입력이면 항상 같은 점수가 나오고, 축마다 근거(측정값)를 함께 돌려준다.
 * 기준값은 반경 1km(카드 수집 범위) 안에서의 절대 기준이다. 전체 단지 대비 백분위는 배치 계산이 필요해 적용하지 않았다.
 */
public final class ApartmentScorer {

    public static final String METHOD = """
            교통: 가장 가까운 역 200m 이내 8점 ~ 1km 0점(직선) + 1km 안 역이 1곳 늘 때마다 1점(최대 2점)
            교육시설: 가장 가까운 초등학교 300m 이내 5점 ~ 1km 0점 + 1km 안 초·중·고 1곳당 1점(최대 5점). 배정 학교와 무관
            생활편의: 1km 안 대형마트 수(3곳 이상 4점) + 가장 가까운 대형마트 300m 이내 3점 ~ 1km 0점 + 병원 수(60곳 이상 3점)
            공원: 가장 가까운 공원 200m 이내 6점 ~ 1km 0점 + 1km 안 공원 1곳당 1점(최대 4점)
            시세 흐름: 최근 1년 평당가의 직전 1년 대비 변화율, 0%가 5점이고 ±10%에서 10점/0점. 두 기간 중 거래가 없으면 계산하지 않음
            """.strip();

    /** 축별 점수(null이면 데이터 없음)와 근거 */
    public record Scores(Double transportation, Double education, Double convenience, Double park, Double priceTrend,
                         Map<String, String> evidence) {}

    private ApartmentScorer() {
    }

    public static Scores score(ApartmentFacts facts) {
        Map<String, String> evidence = new LinkedHashMap<>();
        Nearby n = facts.nearby();

        Double transportation = null, education = null, convenience = null, park = null;
        if (n.available()) {
            transportation = transportation(n.stations(), evidence);
            education = education(n, evidence);
            convenience = convenience(n, evidence);
            park = park(n.parks(), evidence);
        } else {
            evidence.put("transportation", "주변 시설 조회 실패");
        }
        Double priceTrend = priceTrend(facts.deals(), evidence);
        return new Scores(transportation, education, convenience, park, priceTrend, evidence);
    }

    private static double transportation(List<Spot> stations, Map<String, String> evidence) {
        if (stations.isEmpty()) {
            evidence.put("transportation", "1km 안 지하철역 없음");
            return 0;
        }
        Spot nearest = stations.get(0);
        double score = linear(nearest.distance(), 200, 1000, 8) + Math.min(stations.size() - 1, 2);
        evidence.put("transportation", nearest.name() + " " + nearest.distance() + "m, 1km 안 역 " + stations.size() + "곳");
        return round(score);
    }

    private static double education(Nearby n, Map<String, String> evidence) {
        int schools = n.elementarySchools().size() + n.middleSchools().size() + n.highSchools().size();
        double score = Math.min(schools, 5);
        String nearestText = "초등학교 없음";
        if (!n.elementarySchools().isEmpty()) {
            Spot nearest = n.elementarySchools().get(0);
            score += linear(nearest.distance(), 300, 1000, 5);
            nearestText = nearest.name() + " " + nearest.distance() + "m";
        }
        evidence.put("education", nearestText + ", 1km 안 초·중·고 " + schools + "곳");
        return round(score);
    }

    private static double convenience(Nearby n, Map<String, String> evidence) {
        double score = Math.min(n.martCount(), 3) / 3.0 * 4 + Math.min(n.hospitalCount(), 60) / 60.0 * 3;
        String nearestText = "대형마트 없음";
        if (!n.marts().isEmpty()) {
            Spot nearest = n.marts().get(0);
            score += linear(nearest.distance(), 300, 1000, 3);
            nearestText = nearest.name() + " " + nearest.distance() + "m";
        }
        evidence.put("convenience", nearestText + ", 대형마트 " + n.martCount() + "곳, 병원 " + n.hospitalCount() + "곳");
        return round(score);
    }

    private static double park(List<Spot> parks, Map<String, String> evidence) {
        if (parks.isEmpty()) {
            evidence.put("park", "1km 안 공원 없음");
            return 0;
        }
        Spot nearest = parks.get(0);
        double score = linear(nearest.distance(), 200, 1000, 6) + Math.min(parks.size(), 4);
        evidence.put("park", nearest.name() + " " + nearest.distance() + "m, 1km 안 공원 " + parks.size() + "곳");
        return round(score);
    }

    private static Double priceTrend(ApartmentFacts.DealSummary deals, Map<String, String> evidence) {
        Double change = deals.pricePerPyungChangePercent();
        if (change == null) {
            evidence.put("priceTrend", "비교할 거래 부족 (최근 1년 " + deals.dealCount() + "건, 직전 1년 " + deals.prevDealCount() + "건)");
            return null;
        }
        evidence.put("priceTrend", String.format("평당 %,d만원 → %,d만원 (%+.1f%%, 거래 %d건 → %d건)",
                deals.prevPricePerPyung(), deals.pricePerPyung(), change, deals.prevDealCount(), deals.dealCount()));
        return round(Math.max(0, Math.min(10, 5 + change / 2)));
    }

    /** distance가 full 이하면 max점, zero 이상이면 0점, 사이는 직선 보간 */
    static double linear(int distance, int full, int zero, double max) {
        if (distance <= full) {
            return max;
        }
        if (distance >= zero) {
            return 0;
        }
        return max * (zero - distance) / (double) (zero - full);
    }

    private static double round(double value) {
        return Math.round(Math.min(value, 10) * 10) / 10.0;
    }
}
