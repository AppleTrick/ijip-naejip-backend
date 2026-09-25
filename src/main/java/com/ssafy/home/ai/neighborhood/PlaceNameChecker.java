package com.ssafy.home.ai.neighborhood;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LLM 답변에 나온 역·학교 이름이 근거(단지 정보 카드)에 있는지 대조한다.
 * 근거에 없는 이름이 나오면 기억으로 지어낸 것으로 보고 답변을 다시 만든다.
 */
public final class PlaceNameChecker {

    private static final Pattern PLACE = Pattern.compile("([가-힣A-Za-z0-9]+(?:역|초등학교|중학교|고등학교))");

    /** 역으로 끝나지만 역 이름이 아닌 말 */
    private static final Set<String> NON_STATION_SUFFIXES = Set.of("지역", "구역", "영역", "권역", "광역", "전역", "무역", "해역", "역역");

    /** 특정 장소가 아닌 일반 명사 */
    private static final Set<String> GENERIC_WORDS = Set.of(
            "지하철역", "전철역", "기차역", "인근역", "주변역", "환승역", "근처역", "가까운역",
            "초등학교", "중학교", "고등학교", "인근초등학교", "주변초등학교");

    private PlaceNameChecker() {
    }

    public static Set<String> findUnknownPlaces(String answer, Set<String> knownNames) {
        Set<String> unknown = new LinkedHashSet<>();
        if (answer == null) {
            return unknown;
        }
        Matcher matcher = PLACE.matcher(answer);
        while (matcher.find()) {
            String name = matcher.group(1);
            if (name.endsWith("역") && (name.length() < 3 || NON_STATION_SUFFIXES.stream().anyMatch(name::endsWith))) {
                continue;
            }
            if (GENERIC_WORDS.contains(name)) {
                continue;
            }
            if (!isKnown(name, knownNames)) {
                unknown.add(name);
            }
        }
        return unknown;
    }

    /** "염리초등학교"와 "서울염리초등학교"처럼 앞의 시 이름만 다른 경우를 같은 곳으로 본다 */
    private static boolean isKnown(String name, Set<String> knownNames) {
        for (String known : knownNames) {
            if (known.equals(name) || known.endsWith(name) || name.endsWith(known)) {
                return true;
            }
        }
        return false;
    }
}
