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

    /** 거부 사유의 유형 (": " 앞부분) — 메트릭 라벨로 쓰이고, 기동 시 0으로 미리 등록된다 */
    public static final List<String> REJECT_TYPES = List.of(
            "empty query", "only SELECT statements are allowed", "multiple statements are not allowed",
            "comments are not allowed", "forbidden keyword", "table not allowed");

    private static final String ALLOWED_SCHEMA = "ijip_db";

    private static final Pattern STRING_LITERAL = Pattern.compile("'(?:[^'\\\\]|\\\\.|'')*'|\"(?:[^\"\\\\]|\\\\.|\"\")*\"");

    private static final Pattern COMMENT = Pattern.compile("--|/\\*|\\*/|#");

    private static final Pattern FORBIDDEN = Pattern.compile(
            "\\b(SLEEP|BENCHMARK|LOAD_FILE|OUTFILE|DUMPFILE|INTO|GET_LOCK|RELEASE_LOCK|FOR\\s+UPDATE|LOCK\\s+IN\\s+SHARE\\s+MODE"
                    + "|INFORMATION_SCHEMA|PERFORMANCE_SCHEMA|MYSQL|SYS)\\b|@@",
            Pattern.CASE_INSENSITIVE);

    /** 뒤에 테이블 목록이 오는 키워드 */
    private static final Pattern TABLE_LIST_KEYWORD = Pattern.compile("\\b(?:FROM|STRAIGHT_JOIN|JOIN)\\b", Pattern.CASE_INSENSITIVE);

    /** 테이블 목록이 끝났음을 알리는 키워드 (별칭과 구분) */
    private static final Pattern CLAUSE_KEYWORD = Pattern.compile(
            "(?i)WHERE|GROUP|ORDER|LIMIT|HAVING|JOIN|INNER|LEFT|RIGHT|OUTER|CROSS|STRAIGHT_JOIN|NATURAL|UNION|WINDOW"
                    + "|ON|USING|USE|FORCE|IGNORE|PARTITION|FOR|LOCK|INTO");

    private static final Pattern SUBQUERY_START = Pattern.compile("(?i)SELECT\\b");

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

    /**
     * FROM·JOIN 뒤의 테이블 참조를 모두 모은다.
     * 서브쿼리 `(SELECT ...)`는 괄호 짝을 맞춰 통째로 건너뛰고(안쪽 FROM은 이 반복문이 따로 검사),
     * 그 뒤의 `, 다른테이블`까지 계속 읽는다.
     */
    private List<String> referencedTables(String sql) {
        List<String> tables = new ArrayList<>();
        Matcher keyword = TABLE_LIST_KEYWORD.matcher(sql);
        while (keyword.find()) {
            if (isFunctionArgument(sql, keyword.start())) {
                continue;
            }
            collectTableList(sql.substring(keyword.end()), tables);
        }
        return tables;
    }

    /** "t1 a, (SELECT ...) b, (t2) c WHERE ..." 형태에서 테이블 이름만 모은다 */
    private void collectTableList(String s, List<String> tables) {
        int i = 0;
        int n = s.length();
        while (true) {
            i = skipWhitespace(s, i);
            if (i >= n) {
                return;
            }
            if (s.charAt(i) == '(') {
                int close = matchingParen(s, i);
                if (close < 0) {
                    return; // 괄호 불균형 — 문법 오류라 DB가 거부한다
                }
                String inner = s.substring(i + 1, close).trim();
                if (!SUBQUERY_START.matcher(inner).lookingAt()) {
                    collectTableList(inner, tables); // (houseinfos)처럼 괄호로 감싼 테이블 참조
                }
                i = close + 1;
            } else {
                int end = wordEnd(s, i);
                tables.add(s.substring(i, end));
                i = end;
            }

            // 별칭(AS x)을 건너뛰고, ','면 다음 항목, 절 키워드나 괄호면 목록 끝
            while (true) {
                i = skipWhitespace(s, i);
                if (i >= n) {
                    return;
                }
                char c = s.charAt(i);
                if (c == ',') {
                    i++;
                    break;
                }
                if (c == '(' || c == ')') {
                    return;
                }
                int end = wordEnd(s, i);
                if (CLAUSE_KEYWORD.matcher(s.substring(i, end)).matches()) {
                    return;
                }
                i = end;
            }
        }
    }

    private static int skipWhitespace(String s, int i) {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
        return i;
    }

    private static int wordEnd(String s, int i) {
        while (i < s.length() && !Character.isWhitespace(s.charAt(i)) && ",()".indexOf(s.charAt(i)) < 0) {
            i++;
        }
        return i;
    }

    /** open 위치의 '('와 짝인 ')' 위치 (없으면 -1) */
    private static int matchingParen(String s, int open) {
        int depth = 0;
        for (int i = open; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')' && --depth == 0) {
                return i;
            }
        }
        return -1;
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
