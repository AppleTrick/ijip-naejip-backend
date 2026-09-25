package com.ssafy.home.ai.neighborhood;

import com.ssafy.home.ai.neighborhood.ApartmentFacts.DealSummary;
import com.ssafy.home.ai.neighborhood.ApartmentFacts.Nearby;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApartmentScorerTest {

    @Test
    void 같은_입력이면_같은_점수와_근거를_낸다() {
        ApartmentFacts facts = ApartmentFactsTest.sampleFacts();

        assertThat(ApartmentScorer.score(facts)).isEqualTo(ApartmentScorer.score(facts));
    }

    @Test
    void 한화오벨리스크_실측값으로_계산한_점수() {
        ApartmentScorer.Scores s = ApartmentScorer.score(ApartmentFactsTest.sampleFacts());

        // 마포역 60m → 8점 + 1km 안 역 2곳 → 1점
        assertThat(s.transportation()).isEqualTo(9.0);
        // 염리초 307m → 4.95점 + 초·중·고 1곳 → 0.33점
        assertThat(s.education()).isEqualTo(5.3);
        // 대형마트 4곳 → 3.2점 + 380m → 2.66점 + 병원 173곳 → 2.6점
        assertThat(s.convenience()).isEqualTo(8.5);
        // 삼개어린이공원 77m → 6점 + 공원 1곳 → 0.4점
        assertThat(s.park()).isEqualTo(6.4);
        // 평당 4,958 → 5,075만원 (+2.4%) → 5 + 0.39
        assertThat(s.priceTrend()).isEqualTo(5.4);
        assertThat(s.evidence().get("transportation")).isEqualTo("마포역 60m, 1km 안 역 2곳");
        assertThat(s.evidence().get("priceTrend")).contains("+2.4%");
    }

    @Test
    void 비교할_거래가_없으면_시세_흐름은_계산하지_않는다() {
        ApartmentFacts base = ApartmentFactsTest.sampleFacts();
        DealSummary noPrev = new DealSummary(base.deals().from(), base.deals().to(), 70, 84456L, 5075L, "마포구", 2014, 6570L, 0, null);
        ApartmentFacts facts = new ApartmentFacts(base.aptSeq(), base.aptName(), base.region(), base.buildYear(),
                base.latitude(), base.longitude(), noPrev, base.nearby(), base.collectedAt());

        ApartmentScorer.Scores s = ApartmentScorer.score(facts);

        assertThat(s.priceTrend()).isNull();
        assertThat(s.evidence().get("priceTrend")).startsWith("비교할 거래 부족");
    }

    @Test
    void 주변_시설이_없거나_조회에_실패하면_지어내지_않는다() {
        ApartmentFacts base = ApartmentFactsTest.sampleFacts();
        Nearby empty = new Nearby(true, List.of(), List.of(), List.of(), List.of(), List.of(), 0, 0, 0, 0, List.of());
        ApartmentFacts facts = new ApartmentFacts(base.aptSeq(), base.aptName(), base.region(), base.buildYear(),
                base.latitude(), base.longitude(), base.deals(), empty, LocalDate.of(2026, 9, 25));

        ApartmentScorer.Scores none = ApartmentScorer.score(facts);
        assertThat(none.transportation()).isZero();
        assertThat(none.park()).isZero();

        ApartmentFacts failed = new ApartmentFacts(base.aptSeq(), base.aptName(), base.region(), base.buildYear(),
                base.latitude(), base.longitude(), base.deals(), Nearby.unavailable(), LocalDate.of(2026, 9, 25));
        assertThat(ApartmentScorer.score(failed).transportation()).isNull();
    }

    @Test
    void 거리_점수는_구간_사이를_직선으로_보간한다() {
        assertThat(ApartmentScorer.linear(200, 200, 1000, 8)).isEqualTo(8);
        assertThat(ApartmentScorer.linear(600, 200, 1000, 8)).isEqualTo(4);
        assertThat(ApartmentScorer.linear(1000, 200, 1000, 8)).isZero();
    }
}
