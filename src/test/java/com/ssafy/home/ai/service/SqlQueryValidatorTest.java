package com.ssafy.home.ai.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class SqlQueryValidatorTest {

    private final SqlQueryValidator validator = new SqlQueryValidator();

    @ParameterizedTest
    @ValueSource(strings = {
            // 시스템 프롬프트의 대표 쿼리 패턴
            "SELECT h.apt_nm, d.dong_name, ROUND(s.avg_price / 10000, 2) AS avg_price_억원, SUM(s.deal_count) as total_deals, s.pyung "
                    + "FROM houseinfos h JOIN dongcodes d ON h.dong_code = d.dong_code "
                    + "JOIN apt_pyung_stats s ON h.apt_seq = s.apt_seq AND s.is_representative = 1 "
                    + "WHERE d.gugun_name = '마포구' GROUP BY h.apt_nm, d.dong_name, s.avg_price, s.pyung ORDER BY s.avg_price DESC LIMIT 5",
            "SELECT h.apt_seq, h.apt_nm FROM houseinfos h LEFT JOIN apt_pyung_stats s ON h.apt_seq = s.apt_seq AND s.is_representative = 1 WHERE h.build_year > 2010",
            "SELECT COUNT(*) FROM housedeals WHERE apt_seq = '11710-5715';",
            "select avg(deal_amount) from ijip_db.housedeals",
            "SELECT * FROM `houseinfos`",
            "SELECT a, b FROM houseinfos h, dongcodes d WHERE h.dong_code = d.dong_code",
            "SELECT EXTRACT(YEAR FROM deal_date) y, COUNT(*) FROM housedeals GROUP BY y",
            "SELECT SUBSTRING(dong_code FROM 1 FOR 2) FROM dongcodes",
            "SELECT * FROM houseinfos WHERE apt_nm LIKE '%from users%'",
            "SELECT * FROM houseinfos WHERE apt_seq IN (SELECT apt_seq FROM apt_pyung_stats WHERE pyung = 30)",
            "SELECT x.cnt FROM (SELECT COUNT(*) cnt FROM housedeals) x",
            "SELECT * FROM dongcodes WHERE dong_name = 'O''Neil'"
    })
    void 허용_테이블에_대한_단일_SELECT는_통과한다(String sql) {
        assertThat(validator.rejectReason(sql)).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // 사적 데이터·계정 정보
            "SELECT * FROM ai_reports",
            "SELECT * FROM users",
            "SELECT * FROM user_houses",
            "SELECT user, authentication_string FROM mysql.user",
            "SELECT * FROM information_schema.tables",
            "SELECT * FROM performance_schema.threads",
            "SELECT * FROM sys.sessions",
            "SELECT * FROM other_db.members",
            "SELECT * FROM houseinfos h JOIN ai_reports r ON 1 = 1",
            "SELECT * FROM houseinfos WHERE apt_seq IN (SELECT user_id FROM ai_reports)",
            "SELECT * FROM houseinfos, ai_reports",
            // 부하·파일·잠금
            "SELECT SLEEP(30)",
            "SELECT BENCHMARK(100000000, MD5('a'))",
            "SELECT LOAD_FILE('/etc/passwd')",
            "SELECT * FROM houseinfos INTO OUTFILE '/tmp/x'",
            "SELECT * FROM houseinfos FOR UPDATE",
            "SELECT GET_LOCK('a', 10)",
            "SELECT @@version",
            // 다중 문·주석·SELECT 외 문
            "SELECT 1; DROP TABLE houseinfos",
            "SELECT * FROM houseinfos -- comment",
            "SELECT * FROM houseinfos /* x */",
            "SELECT * FROM houseinfos # x",
            "DELETE FROM houseinfos",
            "WITH t AS (SELECT * FROM ai_reports) SELECT * FROM t",
            "",
            "   "
    })
    void 허용되지_않은_쿼리는_거부한다(String sql) {
        assertThat(validator.rejectReason(sql)).isNotNull();
    }

    @Test
    void null은_거부한다() {
        assertThat(validator.isSafeQuery(null)).isFalse();
    }
}
