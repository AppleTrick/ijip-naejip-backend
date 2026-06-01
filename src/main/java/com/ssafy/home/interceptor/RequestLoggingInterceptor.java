package com.ssafy.home.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME = "requestStartTime";
    private static final long WARN_THRESHOLD_MS = 2_000;
    private static final long INFO_THRESHOLD_MS =   500;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Long start = (Long) request.getAttribute(START_TIME);
        if (start == null) return;

        long elapsed = System.currentTimeMillis() - start;
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String label = query != null ? uri + "?" + query : uri;

        if (elapsed >= WARN_THRESHOLD_MS) {
            log.warn("[API] {}ms | {} {}", elapsed, request.getMethod(), label);
        } else if (elapsed >= INFO_THRESHOLD_MS) {
            log.info("[API] {}ms | {} {}", elapsed, request.getMethod(), label);
        } else {
            log.debug("[API] {}ms | {} {}", elapsed, request.getMethod(), label);
        }
    }
}
