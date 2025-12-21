package com.ssafy.home.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class AIConfig {

    @Bean
    public org.springframework.web.client.RestClient.Builder restClientBuilder() {
        return org.springframework.web.client.RestClient.builder()
                .requestInterceptor((request, body, execution) -> {
                    // Cloudflare 등을 우회하기 위해 일반적인 브라우저 User-Agent 추가
                    request.getHeaders().set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                    
                    log.info(">>> [AI REQUEST] {} {}", request.getMethod(), request.getURI());
                    log.info(">>> [AI HEADERS] {}", request.getHeaders());
                    log.info(">>> [AI BODY] {}", new String(body, java.nio.charset.StandardCharsets.UTF_8));
                    
                    var response = execution.execute(request, body);
                    
                    log.info("<<< [AI RESPONSE STATUS] {}", response.getStatusCode());
                    return response;
                });
    }
}
