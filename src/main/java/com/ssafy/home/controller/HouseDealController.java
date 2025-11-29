package com.ssafy.home.controller;

import com.ssafy.home.dto.HouseDealDto;
import com.ssafy.home.service.HouseDealService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping({"/api/house", "/api/v1/house"})
@RequiredArgsConstructor
public class HouseDealController {

    private final HouseDealService houseDealService;

    /**
     * 특정 동코드의 최근 거래 내역 조회
     * GET /api/house/deals/dong/{dongCode}?limit=10
     */
    @GetMapping("/deals/dong/{dongCode}")
    public ResponseEntity<List<HouseDealDto>> getDealsByDongCode(
            @PathVariable("dongCode") String dongCode,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        log.info("동코드별 거래 내역 조회 요청 - 동코드: {}, 개수: {}", dongCode, limit);
        List<HouseDealDto> deals = houseDealService.getRecentDealsByDongCode(dongCode, limit);
        log.info("동코드별 거래 내역 조회 완료: {} 개", deals.size());
        return ResponseEntity.ok(deals);
    }

    /**
     * 특정 아파트의 모든 거래 내역 조회
     * GET /api/house/deals/apt/{aptSeq}
     */
    @GetMapping("/deals/apt/{aptSeq}")
    public ResponseEntity<List<HouseDealDto>> getDealsByAptSeq(
            @PathVariable("aptSeq") String aptSeq) {
        log.info("아파트별 거래 내역 조회 요청 - aptSeq: {}", aptSeq);
        List<HouseDealDto> deals = houseDealService.getDealsByAptSeq(aptSeq);
        log.info("아파트별 거래 내역 조회 완료: {} 개", deals.size());
        return ResponseEntity.ok(deals);
    }

    /**
     * 특정 거래 내역 상세 조회
     * GET /api/house/deals/{no}
     */
    @GetMapping("/deals/{no}")
    public ResponseEntity<HouseDealDto> getDealByNo(@PathVariable("no") Integer no) {
        log.info("거래 상세 조회 요청 - no: {}", no);
        HouseDealDto deal = houseDealService.getDealByNo(no);

        if (deal == null) {
            log.warn("거래 내역을 찾을 수 없음 - no: {}", no);
            return ResponseEntity.notFound().build();
        }

        log.info("거래 상세 조회 완료 - no: {}", no);
        return ResponseEntity.ok(deal);
    }
    /**
     * 지도 영역(Bounds) 내의 거래 내역 조회
     * GET /api/house/deals/bounds?minLat=...&maxLat=...&minLng=...&maxLng=...
     */
    @GetMapping("/deals/bounds")
    public ResponseEntity<List<HouseDealDto>> getDealsByBounds(
            @RequestParam("minLat") double minLat,
            @RequestParam("maxLat") double maxLat,
            @RequestParam("minLng") double minLng,
            @RequestParam("maxLng") double maxLng,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        log.info("지도 영역별 거래 내역 조회 요청 - minLat: {}, maxLat: {}, minLng: {}, maxLng: {}, limit: {}", minLat, maxLat, minLng, maxLng, limit);
        List<HouseDealDto> deals = houseDealService.getHouseDealsByBounds(minLat, maxLat, minLng, maxLng, limit);
        log.info("지도 영역별 거래 내역 조회 완료: {} 개", deals.size());
        return ResponseEntity.ok(deals);
    }
}

