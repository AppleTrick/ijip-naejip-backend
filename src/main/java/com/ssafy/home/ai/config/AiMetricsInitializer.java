package com.ssafy.home.ai.config;

import com.ssafy.home.ai.service.GroundedLlm;
import com.ssafy.home.ai.service.PriceUnitChecker;
import com.ssafy.home.ai.service.SqlQueryValidator;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;

/**
 * AI 서비스 카운터를 기동 시 0으로 미리 만든다.
 *
 * 카운터는 첫 사건 때 생기는데, Prometheus의 increase()는 첫 샘플 이전 값을 모르므로
 * 그 첫 증가분을 세지 못한다. 재시작할 때마다 라벨 조합별 첫 1건이 빠지고,
 * 호출이 드문 AI 지표에서는 이 오차가 크다(2026-09-27 실제 7건 → 대시보드 6건).
 */
@Component
public class AiMetricsInitializer {

    public AiMetricsInitializer(MeterRegistry registry,
                                @Value("${spring.ai.openai.base-url}") String baseUrl,
                                @Value("${spring.ai.openai.chat.options.model}") String model,
                                @Value("${ai.chat.fallback-model:openai/gpt-oss-20b}") String fallbackModel) {
        String provider = AIConfig.providerOf(URI.create(baseUrl).getHost());
        for (String m : List.of(model, fallbackModel)) {
            for (String outcome : AIConfig.LLM_OUTCOMES) {
                registry.counter("ijip.ai.llm.requests", "provider", provider, "model", m, "outcome", outcome);
            }
        }
        GroundedLlm.GROUNDING_RESULTS.forEach(r -> registry.counter("ijip.ai.grounding", "result", r));
        SqlQueryValidator.REJECT_TYPES.forEach(r -> registry.counter("ijip.ai.sql.rejections", "reason", r));
        registry.counter("ijip.ai.sql.rejections", "reason", PriceUnitChecker.REJECT_TYPE);
        List.of("hit", "miss").forEach(r -> registry.counter("ijip.ai.facts.cache", "result", r));
    }
}
