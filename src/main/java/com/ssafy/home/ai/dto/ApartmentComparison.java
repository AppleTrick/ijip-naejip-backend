package com.ssafy.home.ai.dto;

import java.util.List;
import java.util.Map;

/**
 * 관심 단지 비교 — 점수는 코드가 측정값으로 계산하고, summary만 LLM이 쓴다
 */
public final class ApartmentComparison {

    private ApartmentComparison() {
    }

    public record Request(List<String> aptSeqs) {}

    /** 축별 점수 0~10, null이면 데이터 없음. evidence는 축 키별 측정값 */
    public record Item(String aptSeq, String name, Double transportation, Double education, Double convenience,
                       Double park, Double priceTrend, Map<String, String> evidence) {}

    /**
     * @param summary 근거 기반 LLM 비교 요약 (실패 시 안내 문구)
     * @param method  점수 계산 기준
     * @param notes   제외된 단지 등 안내
     */
    public record Response(String summary, List<Item> items, String method, List<String> notes) {}
}
