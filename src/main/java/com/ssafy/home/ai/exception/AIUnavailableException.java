package com.ssafy.home.ai.exception;

/**
 * LLM 호출 실패 — 응답을 만들지 못했으므로 리포트로 저장하지 않는다
 */
public class AIUnavailableException extends RuntimeException {

    private final boolean rateLimited;

    public AIUnavailableException(String message, boolean rateLimited, Throwable cause) {
        super(message, cause);
        this.rateLimited = rateLimited;
    }

    public boolean isRateLimited() {
        return rateLimited;
    }
}
