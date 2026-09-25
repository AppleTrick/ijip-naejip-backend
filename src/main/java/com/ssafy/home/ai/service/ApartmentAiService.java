package com.ssafy.home.ai.service;

import com.ssafy.home.ai.dto.ApartmentChatRequest;

/**
 * 단지 단위 AI — 단지 정보 카드(실거래 DB + 카카오 로컬)를 근거로 답한다
 */
public interface ApartmentAiService {

    /** 단지에 대한 자유 질문 */
    String chat(ApartmentChatRequest request);

    /** 입지 장점 3줄 요약 */
    String locationAttraction(String aptSeq);
}
