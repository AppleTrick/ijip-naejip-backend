package com.ssafy.home.ai.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.metrics.micrometer.MicrometerMetricsTrackerFactory;
import io.micrometer.core.instrument.MeterRegistry;
import net.ttddyy.observation.boot.autoconfigure.DataSourceProxyConnectionIdManagerProvider;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import net.ttddyy.observation.tracing.DataSourceObservationListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * AI가 생성한 SQL 전용 실행기
 *
 * 애플리케이션 계정과 분리된 읽기 전용 DB 계정(ai.db.username)으로 별도 커넥션 풀을 연다.
 * 계정에는 허용 테이블 SELECT 권한만 있으므로 검증기를 우회한 쿼리도 DB에서 거부된다.
 * DataSource/JdbcTemplate을 빈으로 등록하지 않는다 — 등록하면 기본 DataSource 자동 설정이 꺼진다.
 */
@Slf4j
@Component
public class AIQueryJdbc implements DisposableBean {

    private static final int QUERY_TIMEOUT_SECONDS = 5;
    private static final int MAX_ROWS = 200;

    private final JdbcTemplate jdbcTemplate;
    private final HikariDataSource readOnlyDataSource;

    public AIQueryJdbc(DataSource applicationDataSource,
                       MeterRegistry meterRegistry,
                       DataSourceObservationListener observationListener,
                       DataSourceProxyConnectionIdManagerProvider connectionIdManagerProvider,
                       @Value("${spring.datasource.url}") String url,
                       @Value("${ai.db.username:}") String username,
                       @Value("${ai.db.password:}") String password) {
        DataSource dataSource;
        if (username.isBlank()) {
            log.warn("ai.db.username 미설정 — AI SQL을 애플리케이션 계정으로 실행합니다. 운영에서는 읽기 전용 계정을 설정하세요.");
            readOnlyDataSource = null;
            dataSource = applicationDataSource;
        } else {
            HikariConfig config = new HikariConfig();
            config.setPoolName("ai-readonly");
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(3);
            config.setReadOnly(true);
            // JDBC 타임아웃과 별개로 MySQL 서버에서도 5초 넘는 SELECT를 중단
            config.setConnectionInitSql("SET SESSION MAX_EXECUTION_TIME=" + QUERY_TIMEOUT_SECONDS * 1000);
            // 빈이 아닌 풀이라 자동 계측이 안 붙는다 → 커넥션 풀 메트릭(hikaricp_*{pool="ai-readonly"})과 쿼리 스팬을 직접 연결
            config.setMetricsTrackerFactory(new MicrometerMetricsTrackerFactory(meterRegistry));
            readOnlyDataSource = new HikariDataSource(config);
            // 자동 설정과 같은 조합: 쿼리 리스너 + 커넥션 획득 리스너(methodListener) + 전역 커넥션 ID.
            // methodListener가 빠지면 쿼리 관측에 DataSource가 비어 HikariJdbcObservationFilter가 NPE → 모든 AI 쿼리 실패
            dataSource = ProxyDataSourceBuilder.create("ai-readonly", readOnlyDataSource)
                    .listener(observationListener)
                    .methodListener(observationListener)
                    .connectionIdManager(connectionIdManagerProvider.get())
                    .build();
            log.info("AI SQL 실행 계정: {} (읽기 전용 풀)", username);
        }

        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
        jdbcTemplate.setMaxRows(MAX_ROWS);
    }

    public JdbcTemplate jdbcTemplate() {
        return jdbcTemplate;
    }

    @Override
    public void destroy() {
        if (readOnlyDataSource != null) {
            readOnlyDataSource.close();
        }
    }
}
