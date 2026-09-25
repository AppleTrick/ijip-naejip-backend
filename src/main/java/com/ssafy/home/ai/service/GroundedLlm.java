package com.ssafy.home.ai.service;

import com.ssafy.home.ai.exception.AIUnavailableException;
import com.ssafy.home.ai.neighborhood.PlaceNameChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 근거 데이터 안에서만 답하게 하는 LLM 호출기.
 * - 메시지는 템플릿 치환 없이 Message 객체로 보낸다 (사용자 입력의 중괄호가 템플릿으로 해석되지 않게)
 * - 분당 토큰 한도(429)에 걸리면 한도가 별도인 fallback 모델로 한 번 더 시도한다
 * - 답변에 근거에 없는 역·학교 이름이 나오면 한 번 다시 생성하고, 그래도 나오면 null을 돌려준다
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GroundedLlm {

    private final ChatClient.Builder chatClientBuilder;

    @Value("${ai.chat.fallback-model:openai/gpt-oss-20b}")
    private String fallbackModel;

    /**
     * @param messages      SystemMessage로 시작하는 대화
     * @param knownPlaces   답변에 등장해도 되는 장소 이름
     * @param functionNames 사용할 도구 이름 (없으면 빈 배열)
     * @return 검사를 통과한 답변, 두 번 모두 근거 밖 장소를 언급하면 null
     */
    public String answer(List<Message> messages, Set<String> knownPlaces, String... functionNames) {
        String answer = call(messages, functionNames);
        Set<String> unknown = PlaceNameChecker.findUnknownPlaces(answer, knownPlaces);
        if (unknown.isEmpty()) {
            return answer;
        }

        log.warn("근거에 없는 장소 언급 {} — 재생성", unknown);
        List<Message> retry = new ArrayList<>(messages);
        retry.add(new SystemMessage("직전 답변에 근거 목록에 없는 장소(" + String.join(", ", unknown)
                + ")가 있었다. 근거 목록에 있는 장소만 언급해서 다시 답하라."));
        String retried = call(retry, functionNames);
        Set<String> stillUnknown = PlaceNameChecker.findUnknownPlaces(retried, knownPlaces);
        if (stillUnknown.isEmpty()) {
            return retried;
        }
        log.warn("재생성 후에도 근거에 없는 장소 언급 {} — 답변 폐기", stillUnknown);
        return null;
    }

    private String call(List<Message> messages, String... functionNames) {
        try {
            return request(messages, null, functionNames);
        } catch (RuntimeException e) {
            if (!isRateLimited(e)) {
                throw new AIUnavailableException("AI 응답 생성 실패", false, e);
            }
            log.warn("기본 모델 한도 초과 — {}로 재시도", fallbackModel);
            try {
                return request(messages, fallbackModel, functionNames);
            } catch (RuntimeException retryError) {
                throw new AIUnavailableException("AI 응답 생성 실패", isRateLimited(retryError), retryError);
            }
        }
    }

    private String request(List<Message> messages, String model, String... functionNames) {
        ChatClient.ChatClientRequestSpec spec = chatClientBuilder.build().prompt().messages(messages);
        if (functionNames.length > 0) {
            spec = spec.functions(functionNames);
        }
        if (model != null) {
            spec = spec.options(OpenAiChatOptions.builder().model(model).build());
        }
        return spec.call().content();
    }

    static boolean isRateLimited(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            String message = t.getMessage();
            if (message != null && (message.contains("429") || message.contains("rate_limit_exceeded"))) {
                return true;
            }
        }
        return false;
    }
}
