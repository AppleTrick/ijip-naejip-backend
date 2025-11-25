package com.ssafy.home.controller;

import com.ssafy.home.dto.DongCodeDto;
import com.ssafy.home.service.DongCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/dongcode")
@RequiredArgsConstructor
public class DongCodeController {

    private final DongCodeService dongCodeService;

    /**
     * 모든 시도 목록 조회
     * GET /api/dongcode/sido
     */
    @GetMapping("/sido")
    public ResponseEntity<List<DongCodeDto>> getSidoList() {
        log.info("시도 목록 조회 요청");
        List<DongCodeDto> sidoList = dongCodeService.getAllSido();
        log.info("시도 목록 조회 완료: {} 개", sidoList.size());
        return ResponseEntity.ok(sidoList);
    }

    /**
     * 특정 시도의 구군 목록 조회
     * GET /api/dongcode/gugun?sido=서울특별시
     */
    @GetMapping("/gugun")
    public ResponseEntity<List<DongCodeDto>> getGugunList(@RequestParam("sido") String sidoName) {
        log.info("구군 목록 조회 요청 - 시도: {}", sidoName);
        List<DongCodeDto> gugunList = dongCodeService.getGugunBySido(sidoName);
        log.info("구군 목록 조회 완료: {} 개", gugunList.size());
        return ResponseEntity.ok(gugunList);
    }

    /**
     * 특정 시도, 구군의 동 목록 조회
     * GET /api/dongcode/dong?sido=서울특별시&gugun=강남구
     */
    @GetMapping("/dong")
    public ResponseEntity<List<DongCodeDto>> getDongList(
            @RequestParam("sido") String sidoName,
            @RequestParam("gugun") String gugunName) {
        log.info("동 목록 조회 요청 - 시도: {}, 구군: {}", sidoName, gugunName);
        List<DongCodeDto> dongList = dongCodeService.getDongByGugun(sidoName, gugunName);
        log.info("동 목록 조회 완료: {} 개", dongList.size());
        return ResponseEntity.ok(dongList);
    }
}

