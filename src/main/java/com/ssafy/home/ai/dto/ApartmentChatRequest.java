package com.ssafy.home.ai.dto;

import java.util.List;

/**
 * 단지 상세의 "AI에게 묻기" 요청
 *
 * @param aptSeq   질문 대상 단지
 * @param message  사용자 질문
 * @param history  이전 대화 (role: user | assistant), 최근 것부터 일부만 사용
 */
public record ApartmentChatRequest(String aptSeq, String message, List<Turn> history) {

    public record Turn(String role, String content) {}
}
