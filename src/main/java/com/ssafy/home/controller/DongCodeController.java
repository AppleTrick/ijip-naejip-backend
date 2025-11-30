package com.ssafy.home.controller;

import com.ssafy.home.dto.DongCodeResponse;
import com.ssafy.home.service.DongCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping({"/api/v1/dongcode", "/api/dongcode"})
@RequiredArgsConstructor
@Tag(name = "지역코드 API", description = "시도, 구군, 동 정보 조회 API")
public class DongCodeController {
    private final DongCodeService dongCodeService;

    @Operation(summary = "시도 목록 조회", description = "전국 모든 시도 목록을 조회합니다")
    @GetMapping("/sido")
    public ResponseEntity<List<DongCodeResponse>> getSidoList() {
        List<DongCodeResponse> sidoList = dongCodeService.getAllSido();
        log.info("시도 목록 조회 완료: {} 개", sidoList.size());
        return ResponseEntity.ok(sidoList);
    }

    @Operation(summary = "구군 목록 조회", description = "선택한 시도에 해당하는 구군 목록을 조회합니다")
    @GetMapping("/gugun")
    public ResponseEntity<List<DongCodeResponse>> getGugunList(
            @Parameter(description = "시도명 (예: 서울특별시)", required = true)
            @RequestParam("sido") String sidoName) {
        List<DongCodeResponse> gugunList = dongCodeService.getGugunBySido(sidoName);
        log.info("구군 목록 조회 완료: {} 개", gugunList.size());
        return ResponseEntity.ok(gugunList);
    }

    @Operation(summary = "동 목록 조회", description = "선택한 시도와 구군에 해당하는 동 목록을 조회합니다")
    @GetMapping("/dong")
    public ResponseEntity<List<DongCodeResponse>> getDongList(
            @Parameter(description = "시도명 (예: 서울특별시)", required = true)
            @RequestParam("sido") String sidoName,
            @Parameter(description = "구군명 (예: 강남구)", required = true)
            @RequestParam("gugun") String gugunName) {
        List<DongCodeResponse> dongList = dongCodeService.getDongByGugun(sidoName, gugunName);
        log.info("동 목록 조회 완료: {} 개", dongList.size());
        return ResponseEntity.ok(dongList);
    }

    @Operation(summary = "법정동 상세 정보 조회", description = "동코드를 이용하여 해당 법정동의 상세 정보(좌표, 주소 등)를 조회합니다")
    @GetMapping("/{code}")
    public ResponseEntity<DongCodeResponse> getDetail(
            @Parameter(description = "동코드 (예: 1168010100)", required = true)
            @PathVariable("code") String code) {
        log.info("법정동 코드 상세 조회 요청 - code: {}", code);
        DongCodeResponse dto = dongCodeService.getDetail(code);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }
}


