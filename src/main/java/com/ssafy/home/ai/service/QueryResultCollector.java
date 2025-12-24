package com.ssafy.home.ai.service;

import com.ssafy.home.dto.AddressResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tool 실행 결과를 수집하고 AddressResponse로 변환
 */
@Slf4j
@Component
public class QueryResultCollector {

    private final ThreadLocal<List<Map<String, Object>>> sampleApartmentsHolder = ThreadLocal.withInitial(ArrayList::new);

    /**
     * 샘플 아파트 데이터 저장
     */
    public void collectSampleApartments(List<Map<String, Object>> data) {
        if (data != null && !data.isEmpty()) {
            // apt_seq, apt_nm이 있는 경우만 샘플 아파트로 간주
            Map<String, Object> firstRow = data.get(0);
            if (firstRow.containsKey("apt_seq") && firstRow.containsKey("apt_nm")) {
                log.info("Collecting {} sample apartments", data.size());
                sampleApartmentsHolder.get().addAll(data);
            }
        }
    }

    /**
     * 수집된 샘플 아파트를 AddressResponse로 변환
     */
    public List<AddressResponse> getSampleApartments() {
        List<Map<String, Object>> apartments = sampleApartmentsHolder.get();
        List<AddressResponse> results = apartments.stream()
                .map(this::convertToAddressResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("Converted {} apartments to AddressResponse", results.size());
        return results;
    }

    /**
     * 수집된 데이터 초기화
     */
    public void clear() {
        sampleApartmentsHolder.remove();
    }

    /**
     * Map을 AddressResponse로 변환
     */
    private AddressResponse convertToAddressResponse(Map<String, Object> data) {
        try {
            return AddressResponse.builder()
                    .aptSeq(getString(data, "apt_seq"))
                    .aptName(getString(data, "apt_nm"))
                    .dongCode(getString(data, "dong_code"))
                    .sidoName(getString(data, "sido_name"))
                    .gugunName(getString(data, "gugun_name"))
                    .dongName(getString(data, "dong_name"))
                    .aptDong(getString(data, "apt_dong"))
                    .latitude(getDouble(data, "latitude"))
                    .longitude(getDouble(data, "longitude"))
                    .avgPrice(getInteger(data, "avg_price"))
                    .primaryPyung(getInteger(data, "pyung"))
                    .build();
        } catch (Exception e) {
            log.warn("Failed to convert apartment data: {}", data, e);
            return null;
        }
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private Double getDouble(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

