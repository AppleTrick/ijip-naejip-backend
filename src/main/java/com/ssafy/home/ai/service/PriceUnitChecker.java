package com.ssafy.home.ai.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 질문의 "N억"이 SQL 가격 조건에 올바른 단위로 들어갔는지 검사한다.
 *
 * 가격은 만원 단위로 저장된다(20억 = 200000). 모델이 환산을 직접 하다가 자릿수를 틀리면
 * (2026-09-27 운영: "20억~30억" → deal_amount BETWEEN 20000 AND 30000, 즉 2억~3억)
 * 쿼리는 정상 실행되고 틀린 답이 그럴듯하게 나온다. 결과만으로는 알아채기 어려우므로 실행 전에 막는다.
 *
 * 판정: 질문 금액 E(만원)에 대해 조건 값이 E와 10의 거듭제곱배(10~10000배, 1/10~1/10000)만큼 다르고 E와 같은 값은 없을 때 단위 오류.
 * 억 단위 식(_억원 별칭, /10000)과 비교하는 조건은 N 그대로가 정답이다.
 */
public final class PriceUnitChecker {

    /** 메트릭 라벨 (ijip.ai.sql.rejections의 reason) */
    public static final String REJECT_TYPE = "price unit error";

    private static final Pattern EOK_IN_QUESTION = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*억");

    private static final String NUM = "(\\d+(?:\\.\\d+)?(?:\\s*\\*\\s*\\d+(?:\\.\\d+)?)?)";

    private static final Pattern COMPARISON = Pattern.compile(
            "BETWEEN\\s+" + NUM + "\\s+AND\\s+" + NUM + "|(?:>=|<=|>|<|=)\\s*" + NUM, Pattern.CASE_INSENSITIVE);

    private static final Pattern PRICE_COLUMN = Pattern.compile("deal_amount|avg_price", Pattern.CASE_INSENSITIVE);

    private static final Pattern EOK_EXPRESSION = Pattern.compile("억원|/\\s*10000", Pattern.CASE_INSENSITIVE);

    private static final Pattern CONDITION_START = Pattern.compile("\\b(?:WHERE|HAVING|AND|OR|ON)\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern CONDITION_START_NO_AND = Pattern.compile("\\b(?:WHERE|HAVING|OR|ON)\\b", Pattern.CASE_INSENSITIVE);

    private PriceUnitChecker() {
    }

    /**
     * @return 단위 오류 설명 (문제없으면 null)
     */
    public static String check(String question, String sql) {
        if (question == null || sql == null) {
            return null;
        }
        List<Double> expected = new ArrayList<>();
        Matcher q = EOK_IN_QUESTION.matcher(question);
        while (q.find()) {
            expected.add(Double.parseDouble(q.group(1)) * 10000);
        }
        if (expected.isEmpty()) {
            return null;
        }

        // 가격 조건의 값을 만원 기준으로 환산
        List<Double> actual = new ArrayList<>();
        Matcher m = COMPARISON.matcher(sql);
        while (m.find()) {
            String left = leftExpression(sql, m.start(), m.group().regionMatches(true, 0, "BETWEEN", 0, 7));
            if (!PRICE_COLUMN.matcher(left).find()) {
                continue;
            }
            double multiplier = EOK_EXPRESSION.matcher(left).find() ? 10000 : 1;
            for (int g = 1; g <= m.groupCount(); g++) {
                if (m.group(g) != null) {
                    actual.add(evaluate(m.group(g)) * multiplier);
                }
            }
        }

        for (double e : expected) {
            if (actual.stream().anyMatch(a -> same(a, e))) {
                continue;
            }
            for (double a : actual) {
                for (double factor : new double[]{10, 100, 1000, 10000, 0.1, 0.01, 0.001, 0.0001}) {
                    if (same(a, e * factor)) {
                        long eok = Math.round(e / 10000);
                        return REJECT_TYPE + ": " + format(e / 10000) + "억 is " + format(e) + " in 만원 but the query uses "
                                + format(a) + ". Write it as " + eok + " * 10000, or compare the _억원 expression with " + eok;
                    }
                }
            }
        }
        return null;
    }

    /** 비교 연산자 앞의 왼쪽 식 (직전 WHERE/HAVING/AND/OR/ON 이후). BETWEEN이면 그 안의 AND를 건너뛴다 */
    private static String leftExpression(String sql, int end, boolean between) {
        String head = sql.substring(0, end);
        Pattern start = between ? CONDITION_START_NO_AND : CONDITION_START;
        Matcher s = start.matcher(head);
        int from = 0;
        while (s.find()) {
            from = s.end();
        }
        String left = head.substring(from);
        if (between) {
            String[] parts = left.split("(?i)\\bAND\\b");
            left = parts[parts.length - 1];
        }
        return left;
    }

    private static double evaluate(String expr) {
        double value = 1;
        for (String factor : expr.split("\\*")) {
            value *= Double.parseDouble(factor.trim());
        }
        return value;
    }

    private static boolean same(double a, double b) {
        return Math.abs(a - b) < 0.5;
    }

    private static String format(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v);
    }
}
