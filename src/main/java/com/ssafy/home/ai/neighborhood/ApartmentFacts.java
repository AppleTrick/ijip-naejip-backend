package com.ssafy.home.ai.neighborhood;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 단지 정보 카드 — LLM이 답변의 근거로 쓰는 사실 목록.
 * LLM은 학습 시점 이후의 정보와 단지 단위 세부 정보를 모르므로, 최신 사실은 이 카드로만 공급한다.
 */
public record ApartmentFacts(
        String aptSeq,
        String aptName,
        String region,
        Integer buildYear,
        double latitude,
        double longitude,
        DealSummary deals,
        Nearby nearby,
        LocalDate collectedAt
) {

    public static final int RADIUS_METERS = 1000;

    /** 최근 12개월 실거래 요약 (금액 단위: 만원) */
    public record DealSummary(LocalDate from, LocalDate to, long dealCount, Long avgAmount, Long pricePerPyung,
                              String gugunName, long gugunDealCount, Long gugunPricePerPyung,
                              long prevDealCount, Long prevPricePerPyung) {

        /** 직전 1년 대비 평당가 변화율(%). 두 기간 중 하나라도 거래가 없으면 null */
        public Double pricePerPyungChangePercent() {
            if (pricePerPyung == null || prevPricePerPyung == null || prevPricePerPyung == 0 || dealCount == 0 || prevDealCount == 0) {
                return null;
            }
            return (pricePerPyung - prevPricePerPyung) * 100.0 / prevPricePerPyung;
        }
    }

    /** 이름과 직선거리(m) */
    public record Spot(String name, String detail, int distance) {}

    /** 반경 내 주변 시설. available=false면 조회 실패 또는 키 미설정 */
    public record Nearby(boolean available,
                         List<Spot> stations,
                         List<Spot> elementarySchools, List<Spot> middleSchools, List<Spot> highSchools,
                         List<Spot> marts, int martCount,
                         int hospitalCount, int academyCount, int kindergartenCount,
                         List<Spot> parks) {

        static Nearby unavailable() {
            return new Nearby(false, List.of(), List.of(), List.of(), List.of(), List.of(), 0, 0, 0, 0, List.of());
        }
    }

    /** 답변 검사에 쓰는 장소 이름 목록 (역·학교·마트·공원) */
    public Set<String> placeNames() {
        return Stream.of(nearby.stations(), nearby.elementarySchools(), nearby.middleSchools(), nearby.highSchools(),
                        nearby.marts(), nearby.parks())
                .flatMap(List::stream)
                .map(Spot::name)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** LLM 프롬프트에 넣는 카드 텍스트 */
    public String toCardText() {
        StringBuilder card = new StringBuilder();
        card.append("[단지] ").append(aptName).append(" · ").append(region);
        if (buildYear != null) {
            card.append(" · ").append(buildYear).append("년 준공");
        }
        card.append('\n');

        card.append("[시세 — ").append(deals.from()).append(" ~ ").append(deals.to()).append(" 실거래, 전체 평형] ");
        if (deals.dealCount() == 0) {
            card.append("이 기간 거래 없음");
        } else {
            card.append("거래 ").append(deals.dealCount()).append("건, 평균 ").append(eok(deals.avgAmount()))
                    .append(", 평당 ").append(manwon(deals.pricePerPyung()));
            if (deals.gugunPricePerPyung() != null && deals.pricePerPyung() != null && deals.gugunPricePerPyung() > 0) {
                long diff = Math.round((deals.pricePerPyung() - deals.gugunPricePerPyung()) * 100.0 / deals.gugunPricePerPyung());
                card.append(" (").append(deals.gugunName()).append(" 평당 ").append(manwon(deals.gugunPricePerPyung()))
                        .append(" 대비 ").append(Math.abs(diff)).append(diff >= 0 ? "% 높음)" : "% 낮음)");
            }
            Double change = deals.pricePerPyungChangePercent();
            if (change != null) {
                card.append(", 직전 1년 평당 ").append(manwon(deals.prevPricePerPyung()))
                        .append(String.format(" 대비 %+.1f%%", change));
            }
        }
        card.append('\n');

        if (!nearby.available()) {
            card.append("[주변 시설] 조회하지 못함\n");
        } else {
            String radius = "반경 " + RADIUS_METERS + "m, 직선거리";
            card.append("[교통 — ").append(radius).append("] ").append(spots(nearby.stations(), 3, "지하철역 없음")).append('\n');
            card.append("[학교 — ").append(radius).append(", 가까운 학교가 배정 학교는 아님] ")
                    .append("초등학교: ").append(spots(nearby.elementarySchools(), 2, "없음"))
                    .append(" / 중학교: ").append(spots(nearby.middleSchools(), 2, "없음"))
                    .append(" / 고등학교: ").append(spots(nearby.highSchools(), 2, "없음")).append('\n');
            card.append("[생활 — ").append(radius).append("] 대형마트 ").append(nearby.martCount()).append("곳(")
                    .append(spots(nearby.marts(), 2, "없음")).append("), 병원 ").append(nearby.hospitalCount())
                    .append("곳, 학원 ").append(nearby.academyCount()).append("곳, 어린이집·유치원 ")
                    .append(nearby.kindergartenCount()).append("곳\n");
            card.append("[공원 — ").append(radius).append("] ").append(spots(nearby.parks(), 3, "공원 없음")).append('\n');
        }
        card.append("[없는 데이터] 주차, 관리비, 층간소음, 학교 배정, 도보 경로·소요시간, 개발 계획, 학업성취도");
        return card.toString();
    }

    private static String spots(List<Spot> spots, int limit, String emptyText) {
        if (spots.isEmpty()) {
            return emptyText;
        }
        return spots.stream().limit(limit)
                .map(s -> s.name() + (s.detail() == null || s.detail().isEmpty() ? "" : "(" + s.detail() + ")") + " " + s.distance() + "m")
                .collect(Collectors.joining(", "));
    }

    private static String eok(Long manwon) {
        return manwon == null ? "-" : String.format("%.2f억원", manwon / 10000.0);
    }

    private static String manwon(Long manwon) {
        return manwon == null ? "-" : NumberFormat.getInstance(Locale.KOREA).format(manwon) + "만원";
    }
}
