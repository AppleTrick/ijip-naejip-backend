package com.ssafy.home.ai.service;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * SQL 쿼리 보안 검증
 */
@Component
public class SqlQueryValidator {

    private static final List<String> FORBIDDEN_KEYWORDS = List.of("USERS", "USER_", "NOTIFICATION");

    /**
     * SQL 쿼리가 안전한지 검증
     *
     * @param sql 검증할 SQL 쿼리
     * @return 안전하면 true, 그렇지 않으면 false
     */
    public boolean isSafeQuery(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return false;
        }

        String upperSql = sql.trim().toUpperCase();

        // SELECT 문만 허용
        if (!upperSql.startsWith("SELECT")) {
            return false;
        }

        // 금지된 키워드 검증
        for (String forbidden : FORBIDDEN_KEYWORDS) {
            if (upperSql.contains(forbidden)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 금지된 키워드 목록 반환
     */
    public List<String> getForbiddenKeywords() {
        return FORBIDDEN_KEYWORDS;
    }
}

