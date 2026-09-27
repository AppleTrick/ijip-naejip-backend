package com.ssafy.home.ai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.client.RestClientBuilderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Configuration
public class AIConfig {

    private static final ObjectMapper JSON = new ObjectMapper();

    /** 제공자·모델별 남은 한도 (Gauge는 참조가 살아 있어야 값이 유지된다) */
    private final Map<String, AtomicLong> rateLimitRemaining = new ConcurrentHashMap<>();

    /**
     * Spring AI(Groq) 호출용 RestClient.Builder.
     * RestClientBuilderConfigurer를 거쳐야 Spring Boot의 계측(http.client.requests 메트릭, 트레이스 스팬)이 붙는다.
     * RestClient.builder()로 직접 만들면 관측성에서 외부 LLM 호출 구간이 보이지 않는다.
     */
    @Bean
    public org.springframework.web.client.RestClient.Builder restClientBuilder(RestClientBuilderConfigurer configurer,
                                                                          MeterRegistry meterRegistry) {
        return configurer.configure(org.springframework.web.client.RestClient.builder())
                .requestInterceptor((request, body, execution) -> {
                    // Cloudflare 등을 우회하기 위해 일반적인 브라우저 User-Agent 추가
                    request.getHeaders().set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

                    // 헤더는 Authorization(API 키)을 포함하므로 로그에 남기지 않는다
                    log.info(">>> [AI REQUEST] {} {} ({} bytes)", request.getMethod(), request.getURI(), body.length);
                    log.debug(">>> [AI BODY] {}", new String(body, java.nio.charset.StandardCharsets.UTF_8));

                    long start = System.currentTimeMillis();
                    String provider = providerOf(request.getURI().getHost());
                    String model = modelOf(body);
                    org.springframework.http.client.ClientHttpResponse response;
                    try {
                        response = execution.execute(request, body);
                    } catch (IOException e) {
                        countLlmRequest(meterRegistry, provider, model, "io_error");
                        throw e;
                    }
                    int status = response.getStatusCode().value();
                    countLlmRequest(meterRegistry, provider, model,
                            status == 429 ? "rate_limited" : status >= 400 ? "error" : "success");
                    // 남은 한도 헤더: Groq는 x-ratelimit-remaining-tokens (제공자 교체 시 헤더 이름 확인)
                    String remaining = response.getHeaders().getFirst("x-ratelimit-remaining-tokens");
                    if (remaining != null) {
                        rateLimitRemaining.computeIfAbsent(provider + "|" + model, k -> meterRegistry.gauge(
                                "ijip.ai.llm.ratelimit.remaining", Tags.of("provider", provider, "model", model), new AtomicLong()))
                                .set(parseLongOrZero(remaining));
                    }

                    // Groq가 돌려주는 분당 토큰 잔량 — 요청당 토큰 사용량 추적용
                    log.info("<<< [AI RESPONSE STATUS] {} ({}ms, TPM remaining {}/{})", response.getStatusCode(),
                            System.currentTimeMillis() - start,
                            response.getHeaders().getFirst("x-ratelimit-remaining-tokens"),
                            response.getHeaders().getFirst("x-ratelimit-limit-tokens"));
                    return response;
                });
    }

    private static void countLlmRequest(MeterRegistry registry, String provider, String model, String outcome) {
        registry.counter("ijip.ai.llm.requests", "provider", provider, "model", model, "outcome", outcome).increment();
    }

    static String providerOf(String host) {
        if (host == null) return "unknown";
        if (host.contains("groq")) return "groq";
        if (host.contains("anthropic")) return "anthropic";
        if (host.contains("openai")) return "openai";
        return host;
    }

    /** 요청 본문의 model 필드 (대체 모델 재시도를 구분하려고 라벨로 쓴다) */
    static String modelOf(byte[] body) {
        try {
            return JSON.readTree(body).path("model").asText("unknown");
        } catch (Exception e) {
            return "unknown";
        }
    }

    private static long parseLongOrZero(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
