package com.ssafy.home.ai.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI가 생성한 SQL 쿼리 보안 검증
 *
 * 1차 방어선이다. 최종 방어선은 AI 전용 읽기 전용 DB 계정(허용 테이블 SELECT 권한만)이다.
 * - 단일 SELECT 문만 허용 (세미콜론으로 이어 붙인 다중 문, 주석 차단)
 * - FROM / JOIN 대상은 허용 테이블 화이트리스트만 허용 (다른 스키마 접근 차단)
 * - 부하·파일 접근·잠금 함수 차단 (SLEEP, BENCHMARK, LOAD_FILE, INTO OUTFILE 등)
 */
@Component
public class SqlQueryValidator {

    public static final Set<String> ALLOWED_TABLES = Set.of(
            "dongcodes", "houseinfos", "housedeals",
            "apt_dong_stats", "apt_pyung_stats", "apt_dong_pyung_stats");

    private static final String ALLOWED_SCHEMA = "ijip_db";

    private static final Pattern STRING_LITERAL = Pattern.compile("'(?:[^'\\\\]|\\\\.|'')*'|\"(?:[^\"\\\\]|\\\\.|\"\")*\"");

    private static final Pattern COMMENT = Pattern.compile("--|/\\*|\\*/|#");

    private static final Pattern FORBIDDEN = Pattern.compile(
            "\\b(SLEEP|BENCHMARK|LOAD_FILE|OUTFILE|DUMPFILE|INTO|GET_LOCK|RELEASE_LOCK|FOR\\s+UPDATE|LOCK\\s+IN\\s+SHARE\\s+MODE"
                    + "|INFORMATION_SCHEMA|PERFORMANCE_SCHEMA|MYSQL|SYS)\\b|@@",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern FROM_CLAUSE = Pattern.compile(
            "\\bFROM\\s+(.+?)(?=\\b(?:WHERE|GROUP|ORDER|LIMIT|HAVING|JOIN|INNER|LEFT|RIGHT|CROSS|STRAIGHT_JOIN|UNION|WINDOW)\\b|\\)|$)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern JOIN_TARGET = Pattern.compile("\\bJOIN\\s+([^\\s,()]+)", Pattern.CASE_INSENSITIVE);

    /** FROM을 문법으로 쓰는 함수 — EXTRACT(YEAR FROM deal_date) 등의 FROM은 테이블 참조가 아니다 */
    private static final Pattern FROM_FUNCTION = Pattern.compile("(?i)(EXTRACT|TRIM|SUBSTRING|SUBSTR|MID)\\s*$");

    /**
     * SQL 쿼리가 안전한지 검증
     *
     * @param sql 검증할 SQL 쿼리
     * @return 안전하면 true, 그렇지 않으면 false
     */
    public boolean isSafeQuery(String sql) {
        return rejectReason(sql) == null;
    }

    /**
     * 거부 사유 반환 (안전하면 null)
     */
    public String rejectReason(String sql) {
        if (sql == null || sql.isBlank()) {
            return "empty query";
        }

        // 문자열 리터럴 내용은 검사 대상에서 제외 ('%from%' 같은 검색어 오탐 방지)
        String stripped = STRING_LITERAL.matcher(sql).replaceAll("''").trim();
        if (stripped.endsWith(";")) {
            stripped = stripped.substring(0, stripped.length() - 1).trim();
        }

        if (!stripped.regionMatches(true, 0, "SELECT", 0, 6)) {
            return "only SELECT statements are allowed";
        }
        if (stripped.contains(";")) {
            return "multiple statements are not allowed";
        }
        if (COMMENT.matcher(stripped).find()) {
            return "comments are not allowed";
        }
        Matcher forbidden = FORBIDDEN.matcher(stripped);
        if (forbidden.find()) {
            return "forbidden keyword: " + forbidden.group();
        }

        for (String table : referencedTables(stripped)) {
            if (!isAllowedTable(table)) {
                return "table not allowed: " + table;
            }
        }
        return null;
    }

    private List<String> referencedTables(String sql) {
        List<String> tables = new ArrayList<>();

        Matcher from = FROM_CLAUSE.matcher(sql);
        while (from.find()) {
            if (isFunctionArgument(sql, from.start())) {
                continue;
            }
            for (String item : from.group(1).split(",")) {
                String trimmed = item.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("(")) {
                    continue; // 서브쿼리 — 내부 FROM이 따로 검사된다
                }
                tables.add(trimmed.split("\\s+")[0]);
            }
        }

        Matcher join = JOIN_TARGET.matcher(sql);
        while (join.find()) {
            tables.add(join.group(1));
        }
        return tables;
    }

    /** 위치 앞의 가장 안쪽 미닫힘 괄호가 EXTRACT( 같은 함수 호출이면 true */
    private boolean isFunctionArgument(String sql, int position) {
        int depth = 0;
        for (int i = position - 1; i >= 0; i--) {
            char c = sql.charAt(i);
            if (c == ')') {
                depth++;
            } else if (c == '(') {
                if (depth == 0) {
                    return FROM_FUNCTION.matcher(sql.substring(0, i)).find();
                }
                depth--;
            }
        }
        return false;
    }

    private boolean isAllowedTable(String rawTable) {
        String table = rawTable.replace("`", "").toLowerCase();
        int dot = table.indexOf('.');
        if (dot >= 0) {
            if (!table.substring(0, dot).equals(ALLOWED_SCHEMA)) {
                return false;
            }
            table = table.substring(dot + 1);
        }
        return ALLOWED_TABLES.contains(table);
    }
}
