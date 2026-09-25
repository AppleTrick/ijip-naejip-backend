package com.ssafy.home.ai.service;

import com.ssafy.home.ai.dto.RegionInfo;
import com.ssafy.home.dto.DongCodeResponse;
import com.ssafy.home.mapper.DongCodeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 사용자 쿼리에서 지역명을 추출하고 DB에서 검증하는 전처리 컴포넌트
 * 잘못된 지역 정보로 인한 SQL 오류를 방지합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegionValidator {

    private final DongCodeMapper dongCodeMapper;

    // 지역명 토큰 추출 패턴 (한글 지역명)
    private static final Pattern REGION_TOKEN_PATTERN = Pattern.compile(
            "([가-힣]{1,}(?:특별시|광역시|특별자치시|특별자치도|도|시|군|구|읍|면|동|리))"
    );

    /**
     * 사용자 메시지에서 지역명을 추출하고 DB에서 검증
     *
     * @param userMessage 사용자 입력 메시지
     * @return 검증된 지역 정보 리스트
     */
    public List<RegionInfo> extractAndValidateRegions(String userMessage) {
        log.info("Extracting regions from message: {}", userMessage);

        Set<String> tokens = extractRegionTokens(userMessage);
        log.debug("Extracted tokens: {}", tokens);

        List<RegionInfo> validatedRegions = new ArrayList<>();
        Set<String> processedDongCodes = new HashSet<>(); // 중복 방지

        for (String token : tokens) {
            List<DongCodeResponse> matches = dongCodeMapper.searchByRegionToken(token);

            if (!matches.isEmpty()) {
                log.info("Token '{}' matched {} region(s)", token, matches.size());

                // 가장 구체적인 레벨(동 > 구/군 > 시/도) 선택
                DongCodeResponse bestMatch = matches.get(0);

                // 중복 체크 (같은 dong_code는 한 번만)
                if (!processedDongCodes.contains(bestMatch.dongCode())) {
                    RegionInfo regionInfo = convertToRegionInfo(bestMatch);
                    validatedRegions.add(regionInfo);
                    processedDongCodes.add(bestMatch.dongCode());

                    log.info("Validated region: {} (dong_code: {})",
                            regionInfo.getFullAddress(), regionInfo.getDongCode());
                }
            }
        }

        return validatedRegions;
    }

    /**
     * 메시지에서 지역명 후보 토큰 추출
     */
    private Set<String> extractRegionTokens(String message) {
        Set<String> tokens = new LinkedHashSet<>();
        Matcher matcher = REGION_TOKEN_PATTERN.matcher(message);

        while (matcher.find()) {
            String token = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            if (token != null && token.length() >= 2) {
                tokens.add(token);
            }
        }

        return tokens;
    }

    /**
     * DongCodeResponse를 RegionInfo로 변환
     */
    private RegionInfo convertToRegionInfo(DongCodeResponse dc) {
        StringBuilder fullAddress = new StringBuilder();

        if (dc.sidoName() != null) {
            fullAddress.append(dc.sidoName());
        }
        if (dc.gugunName() != null) {
            if (fullAddress.length() > 0) fullAddress.append(" ");
            fullAddress.append(dc.gugunName());
        }
        if (dc.dongName() != null) {
            if (fullAddress.length() > 0) fullAddress.append(" ");
            fullAddress.append(dc.dongName());
        }

        return RegionInfo.builder()
                .dongCode(dc.dongCode())
                .sidoName(dc.sidoName())
                .gugunName(dc.gugunName())
                .dongName(dc.dongName())
                .fullAddress(fullAddress.toString())
                .build();
    }

    /**
     * 검증된 지역 정보를 프롬프트 형식으로 변환
     */
    public String buildRegionContext(List<RegionInfo> regions) {
        if (regions.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder("\n## Verified regions (use these exact names/codes in SQL)\n");
        for (RegionInfo region : regions) {
            context.append(String.format("- %s: dong_code=%s", region.getFullAddress(), region.getDongCode()));
            if (region.getSidoName() != null) {
                context.append(", sido_name=").append(region.getSidoName());
            }
            if (region.getGugunName() != null) {
                context.append(", gugun_name=").append(region.getGugunName());
            }
            if (region.getDongName() != null) {
                context.append(", dong_name=").append(region.getDongName());
            }
            context.append("\n");
        }

        return context.toString();
    }
}

