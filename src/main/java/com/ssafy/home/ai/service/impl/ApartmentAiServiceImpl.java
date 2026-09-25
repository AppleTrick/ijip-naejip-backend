package com.ssafy.home.ai.service.impl;

import com.ssafy.home.ai.dto.ApartmentChatRequest;
import com.ssafy.home.ai.dto.ApartmentComparison;
import com.ssafy.home.ai.exception.AIUnavailableException;
import com.ssafy.home.ai.neighborhood.ApartmentScorer;
import com.ssafy.home.ai.neighborhood.ApartmentFactService;
import com.ssafy.home.ai.neighborhood.ApartmentFacts;
import com.ssafy.home.ai.neighborhood.ApartmentFacts.Spot;
import com.ssafy.home.ai.service.ApartmentAiService;
import com.ssafy.home.ai.service.GroundedLlm;
import com.ssafy.home.ai.service.QueryResultCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * LLM은 학습 시점 이후의 시세·주변 시설을 모르므로, 사실은 전부 단지 정보 카드와 SQL 조회로 공급하고
 * LLM에는 질문 이해와 설명만 맡긴다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApartmentAiServiceImpl implements ApartmentAiService {

    private static final int MAX_HISTORY_TURNS = 4;
    private static final int MAX_TEXT_LENGTH = 1000;
    private static final int MAX_COMPARE = 4;

    private static final String GROUNDING_RULES = """
            규칙:
            - [단지 정보 카드]와 도구 조회 결과에 있는 사실만 말한다. 카드에 없는 장소·시설·수치는 말하지 않는다.
            - 질문에 답할 데이터가 없으면 없다고 먼저 말하고, 카드에 관련된 정보가 있으면 이어서 알려준다.
              예: 배정 학교 → 배정 정보는 없고, 가까운 초등학교는 카드의 이름과 거리를 알려준다.
            - 거리는 직선거리로 "직선 약 690m"처럼 쓴다. 도보 시간은 말하지 않는다.
            - 가까운 학교를 배정 학교라고 말하지 않는다.
            - 투자를 권유하거나 가격을 예측하지 않는다.
            """;

    private final ApartmentFactService apartmentFactService;
    private final GroundedLlm groundedLlm;
    private final QueryResultCollector queryResultCollector;

    @Override
    public String chat(ApartmentChatRequest request) {
        ApartmentFacts facts = apartmentFactService.getFacts(request.aptSeq());

        String system = """
                너는 한 아파트 단지에 대한 질문에 답하는 부동산 도우미다. 오늘은 %s.
                %s
                - 카드보다 자세한 시세(평형별 가격, 월별 추이, 최근 거래 목록)가 필요할 때만 executeDatabaseQuery를 한 번 호출한다.
                  도구 인자는 문자열로 넘긴다. SQL에는 반드시 WHERE apt_seq = '%s'를 넣는다.
                  테이블: housedeals(apt_seq, deal_date INT YYYYMMDD, deal_amount 만원, exclu_use_ar ㎡, pyung, floor).
                  가격은 SQL에서 ROUND(deal_amount/10000, 2)로 억원 변환한다. LIMIT 20 이하.
                - 한국어로 3~6문장, 필요하면 짧은 목록으로 답한다.

                [단지 정보 카드]
                %s
                """.formatted(facts.collectedAt(), GROUNDING_RULES, facts.aptSeq(), facts.toCardText());

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(system));
        messages.addAll(history(request.history()));
        messages.add(new UserMessage(truncate(request.message())));

        String answer;
        try {
            answer = groundedLlm.answer(messages, facts.placeNames(), "executeDatabaseQuery");
        } finally {
            queryResultCollector.clear(); // 도구가 스레드 로컬에 모은 지도용 샘플은 여기서 쓰지 않는다
        }
        if (answer == null) {
            return "확인된 데이터에 없는 장소가 섞여 답변을 표시하지 않았어요. 질문을 조금 바꿔 다시 물어봐 주세요.";
        }
        return answer;
    }

    @Override
    public String locationAttraction(String aptSeq) {
        ApartmentFacts facts = apartmentFactService.getFacts(aptSeq);

        String system = """
                너는 아파트 입지를 요약하는 분석가다.
                %s
                - 카드에서 이 단지의 입지 장점을 최대 3가지 골라 "✨ 키워드: 설명" 한 줄씩 쓴다. 근거가 부족하면 있는 만큼만 쓴다.
                - 설명에는 카드의 이름과 거리·개수를 그대로 쓴다.

                [단지 정보 카드]
                %s
                """.formatted(GROUNDING_RULES, facts.toCardText());

        String answer = groundedLlm.answer(
                List.of(new SystemMessage(system), new UserMessage("이 단지의 입지 장점을 요약해 줘.")),
                facts.placeNames());
        return answer != null ? answer : summarizeWithoutLlm(facts);
    }

    @Override
    public ApartmentComparison.Response compare(List<String> aptSeqs) {
        List<String> notes = new ArrayList<>();
        List<String> targets = aptSeqs.stream().distinct().toList();
        if (targets.size() > MAX_COMPARE) {
            notes.add("비교는 " + MAX_COMPARE + "개 단지까지 합니다. 앞의 " + MAX_COMPARE + "개만 계산했습니다.");
            targets = targets.subList(0, MAX_COMPARE);
        }

        List<ApartmentFacts> factsList = new ArrayList<>();
        List<ApartmentComparison.Item> items = new ArrayList<>();
        for (String aptSeq : targets) {
            ApartmentFacts facts;
            try {
                facts = apartmentFactService.getFacts(aptSeq);
            } catch (IllegalArgumentException e) {
                notes.add(aptSeq + ": 단지 정보(좌표)가 없어 제외했습니다.");
                continue;
            }
            ApartmentScorer.Scores s = ApartmentScorer.score(facts);
            factsList.add(facts);
            items.add(new ApartmentComparison.Item(facts.aptSeq(), facts.aptName(), s.transportation(), s.education(),
                    s.convenience(), s.park(), s.priceTrend(), s.evidence()));
        }
        if (items.isEmpty()) {
            return new ApartmentComparison.Response("비교할 수 있는 단지가 없습니다.", items, ApartmentScorer.METHOD, notes);
        }

        String summary;
        try {
            summary = summarizeComparison(factsList, items);
        } catch (AIUnavailableException e) {
            // 점수는 LLM 없이 계산했으므로 요약만 빠진 채로 돌려준다
            summary = e.isRateLimited() ? "AI 사용량이 많아 요약을 만들지 못했어요. 점수와 근거는 아래와 같아요." : "AI 요약을 만들지 못했어요. 점수와 근거는 아래와 같아요.";
        }
        return new ApartmentComparison.Response(summary, items, ApartmentScorer.METHOD, notes);
    }

    private String summarizeComparison(List<ApartmentFacts> factsList, List<ApartmentComparison.Item> items) {
        StringBuilder data = new StringBuilder();
        Set<String> places = new LinkedHashSet<>();
        for (int i = 0; i < factsList.size(); i++) {
            ApartmentFacts facts = factsList.get(i);
            ApartmentComparison.Item item = items.get(i);
            places.addAll(facts.placeNames());
            data.append("\n## ").append(facts.aptName()).append('\n').append(facts.toCardText()).append('\n')
                    .append(String.format("[점수 0~10] 교통 %s, 교육시설 %s, 생활편의 %s, 공원 %s, 시세 흐름 %s%n",
                            fmt(item.transportation()), fmt(item.education()), fmt(item.convenience()), fmt(item.park()), fmt(item.priceTrend())));
        }

        String system = """
                너는 아파트 단지들을 비교해 설명하는 분석가다. 점수는 이미 코드로 계산돼 있다. 점수를 바꾸거나 새로 매기지 않는다.
                %s
                - 단지별 강점과 약점을 점수와 카드의 근거(이름·거리·개수·평당가)로 설명한다.
                - 어느 단지가 "더 좋다"고 단정하지 말고, 어떤 우선순위(출퇴근, 아이 교육, 공원, 가격)에 어떤 단지가 맞는지로 정리한다.
                - 한국어 마크다운, 단지별 2~3개 불릿 + 마지막에 우선순위별 한 줄 정리.

                [비교 대상]%s
                """.formatted(GROUNDING_RULES, data);

        String answer = groundedLlm.answer(List.of(new SystemMessage(system), new UserMessage("이 단지들을 비교해 줘.")), places);
        return answer != null ? answer : "근거에 없는 장소가 섞여 AI 요약을 표시하지 않았어요. 점수와 근거는 아래와 같아요.";
    }

    private static String fmt(Double score) {
        return score == null ? "데이터 없음" : String.valueOf(score);
    }

    /** LLM 답변이 검사를 통과하지 못했을 때 카드만으로 만드는 요약 */
    static String summarizeWithoutLlm(ApartmentFacts facts) {
        List<String> lines = new ArrayList<>();
        ApartmentFacts.Nearby nearby = facts.nearby();
        if (!nearby.stations().isEmpty()) {
            Spot s = nearby.stations().get(0);
            lines.add("✨ 교통: " + s.name() + (s.detail().isEmpty() ? "" : "(" + s.detail() + ")") + " 직선 " + s.distance() + "m");
        }
        if (!nearby.elementarySchools().isEmpty()) {
            Spot s = nearby.elementarySchools().get(0);
            lines.add("✨ 학교: " + s.name() + " 직선 " + s.distance() + "m");
        }
        if (!nearby.parks().isEmpty()) {
            Spot s = nearby.parks().get(0);
            lines.add("✨ 공원: " + s.name() + " 직선 " + s.distance() + "m");
        }
        return lines.isEmpty() ? "주변 시설 정보를 불러오지 못했어요." : String.join("\n", lines);
    }

    private static List<Message> history(List<ApartmentChatRequest.Turn> turns) {
        if (turns == null || turns.isEmpty()) {
            return List.of();
        }
        List<Message> messages = new ArrayList<>();
        for (ApartmentChatRequest.Turn turn : turns.subList(Math.max(0, turns.size() - MAX_HISTORY_TURNS), turns.size())) {
            String content = truncate(turn.content());
            messages.add("assistant".equals(turn.role()) ? new AssistantMessage(content) : new UserMessage(content));
        }
        return messages;
    }

    private static String truncate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() > MAX_TEXT_LENGTH ? text.substring(0, MAX_TEXT_LENGTH) : text;
    }
}
