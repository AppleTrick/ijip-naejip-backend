package com.ssafy.home.ai.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class PriceUnitCheckerTest {

    @Test
    void 운영에서_나온_자릿수_오류를_잡는다() {
        // 2026-09-27: "20억~30억"을 2억~3억으로 조회해 2.53억 단지를 추천했다
        String sql = "SELECT h.apt_nm FROM housedeals hd JOIN houseinfos h ON hd.apt_seq = h.apt_seq "
                + "WHERE d.gugun_name = '송파구' AND hd.deal_date >= 20250927 AND hd.deal_amount BETWEEN 20000 AND 30000 "
                + "GROUP BY h.apt_seq";
        assertThat(PriceUnitChecker.check("송파구에서 20억이상 30억 이하 아파트 추천해줘", sql))
                .contains("20억 is 200000");
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            // 만원 컬럼에 올바른 값
            "송파구 20억 이상 30억 이하 | WHERE hd.deal_amount BETWEEN 200000 AND 300000",
            "송파구 20억 이상 30억 이하 | WHERE hd.deal_amount BETWEEN 20 * 10000 AND 30 * 10000",
            "강남구 15억 넘는 아파트 | HAVING avg_price > 150000",
            // 억 단위 식과 비교
            "송파구 20억 이상 30억 이하 | HAVING avg_price_억원 BETWEEN 20 AND 30",
            "서초구 25억 이상 | HAVING ROUND(AVG(hd.deal_amount)/10000, 2) >= 25",
            "노원구 3억 이하 | HAVING ROUND(ap.avg_price / 10000, 2) <= 3.00",
            "마포구 5.5억 이하 | WHERE hd.deal_amount <= 55000",
            // 질문에 금액이 없거나, 가격이 아닌 조건의 숫자
            "마포구 아파트 추천 | WHERE hd.deal_amount > 100",
            "송파구 20억 아파트 | WHERE hd.deal_date >= 20250927 AND h.build_year > 2000",
    })
    void 올바른_단위는_통과한다(String question, String sql) {
        assertThat(PriceUnitChecker.check(question, sql)).isNull();
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "강남구 15억 넘는 아파트 | HAVING avg_price > 15000",
            // 프롬프트 개선 후 평가에서 대체 모델(gpt-oss-20b)이 실제로 만든 조건
            "강남구 15억 넘는 아파트 알려줘 | WHERE ap.avg_price > 1500000",
            "노원구 3억 이하 | WHERE hd.deal_amount <= 300000",
            "서초구 25억 이상 | HAVING avg_price_억원 >= 250000",
            "마포구 5억에서 8억 사이 | WHERE hd.deal_amount BETWEEN 5000 AND 8000",
    })
    void 자릿수가_틀리면_거부한다(String question, String sql) {
        assertThat(PriceUnitChecker.check(question, sql)).startsWith("price unit error");
    }
}
