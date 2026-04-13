package com.ssafy.home.ai.service;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Database Query Tool for AI Function Calling
 * AI가 SQL을 실행할 수 있도록 도구를 제공합니다.
 */
@Slf4j
@Component("executeDatabaseQuery")
@RequiredArgsConstructor
public class DatabaseQueryTool implements Function<DatabaseQueryTool.QueryRequest, DatabaseQueryTool.QueryResponse> {

    private final JdbcTemplate jdbcTemplate;
    private final SqlQueryValidator sqlQueryValidator;
    private final QueryResultCollector queryResultCollector;

    /**
     * AI가 SQL 쿼리를 실행하기 위한 요청
     */
    @JsonClassDescription("Execute a SQL SELECT query against the real estate database to retrieve apartment and transaction data")
    public record QueryRequest(
            @JsonProperty(required = true)
            @JsonPropertyDescription("The SQL SELECT query to execute. Must be a valid MySQL SELECT statement. Only SELECT statements are allowed.")
            String sql,

            @JsonProperty
            @JsonPropertyDescription("A brief description of what this query is analyzing (e.g., 'Average prices in Gangnam')")
            String description
    ) {}

    /**
     * SQL 쿼리 실행 결과
     * NOTE: queryResultRows는 쿼리 결과 행 수이며, 실제 거래건수(deal_count)가 아닙니다.
     * 실제 거래건수는 반드시 SQL 내 SUM(deal_count) 또는 COUNT(*)로 직접 조회하세요.
     */
    public record QueryResponse(
            boolean success,
            String message,
            List<Map<String, Object>> data,
            String queryType  // "statistics" or "sample_apartments"
    ) {}

    @Override
    public QueryResponse apply(QueryRequest request) {
        String sql = request.sql();
        String description = request.description();

        log.info("Tool Called - Description: {}", description);
        log.info("Tool Called - SQL: {}", sql);

        // 보안 검증
        if (!sqlQueryValidator.isSafeQuery(sql)) {
            log.warn("Unsafe query rejected: {}", sql);
            return new QueryResponse(
                    false,
                    "Query rejected: Only SELECT statements on allowed tables are permitted.",
                    Collections.emptyList(),
                    "unknown"
            );
        }

        try {
            // 쿼리 실행
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
            log.info("Query executed successfully. Rows: {}", result.size());

            // 쿼리 타입 자동 감지
            String queryType = detectQueryType(sql, description, result);
            log.info("Detected query type: {}", queryType);

            // 샘플 아파트 데이터인 경우 수집
            if ("sample_apartments".equals(queryType)) {
                queryResultCollector.collectSampleApartments(result);
            }

            return new QueryResponse(
                    true,
                    "Query executed successfully. 실제 거래건수는 data 내 deal_count 또는 COUNT(*) 컬럼을 사용하세요.",
                    result,
                    queryType
            );
        } catch (Exception e) {
            log.error("Query execution failed: {}", sql, e);
            return new QueryResponse(
                    false,
                    "Query execution failed: " + e.getMessage(),
                    Collections.emptyList(),
                    "unknown"
            );
        }
    }

    /**
     * 쿼리 타입 감지 (통계 쿼리 vs 샘플 아파트 쿼리)
     */
    private String detectQueryType(String sql, String description, List<Map<String, Object>> result) {
        String descLower = description != null ? description.toLowerCase() : "";

        // description에 명시적으로 "sample" 키워드가 있으면
        if (descLower.contains("sample") || descLower.contains("예시") || descLower.contains("아파트")) {
            // apt_seq, apt_nm 등 아파트 상세 컬럼이 있는지 확인
            if (!result.isEmpty()) {
                Map<String, Object> firstRow = result.get(0);
                if (firstRow.containsKey("apt_seq") && firstRow.containsKey("apt_nm")) {
                    return "sample_apartments";
                }
            }
        }

        // 결과 구조로 판단
        if (!result.isEmpty()) {
            Map<String, Object> firstRow = result.get(0);
            // apt_seq, apt_nm, latitude, longitude 등이 모두 있으면 샘플 아파트
            boolean hasAptSeq = firstRow.containsKey("apt_seq");
            boolean hasAptNm = firstRow.containsKey("apt_nm");
            boolean hasLatLng = firstRow.containsKey("latitude") || firstRow.containsKey("longitude");

            if (hasAptSeq && hasAptNm && hasLatLng) {
                return "sample_apartments";
            }
        }

        // 기본값은 통계 쿼리
        return "statistics";
    }
}
