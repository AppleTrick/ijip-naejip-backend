package com.ssafy.home.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AIConfig {

    @Bean
    public org.springframework.web.client.RestClient.Builder restClientBuilder() {
        return org.springframework.web.client.RestClient.builder()
                .requestInterceptor((request, body, execution) -> {
                    // Cloudflare 등을 우회하기 위해 일반적인 브라우저 User-Agent 추가
                    request.getHeaders().set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

                    // 헤더는 Authorization(API 키)을 포함하므로 로그에 남기지 않는다
                    log.info(">>> [AI REQUEST] {} {} ({} bytes)", request.getMethod(), request.getURI(), body.length);
                    log.debug(">>> [AI BODY] {}", new String(body, java.nio.charset.StandardCharsets.UTF_8));

                    long start = System.currentTimeMillis();
                    var response = execution.execute(request, body);

                    // Groq가 돌려주는 분당 토큰 잔량 — 요청당 토큰 사용량 추적용
                    log.info("<<< [AI RESPONSE STATUS] {} ({}ms, TPM remaining {}/{})", response.getStatusCode(),
                            System.currentTimeMillis() - start,
                            response.getHeaders().getFirst("x-ratelimit-remaining-tokens"),
                            response.getHeaders().getFirst("x-ratelimit-limit-tokens"));
                    return response;
                });
    }
}
