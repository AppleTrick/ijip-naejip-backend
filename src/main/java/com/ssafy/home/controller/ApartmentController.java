package com.ssafy.home.controller;

import com.ssafy.home.dto.ApartmentDetailResponse;
import com.ssafy.home.dto.CommonResponse;
import com.ssafy.home.service.ApartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(name = "Apartment API (아파트 정보)", description = "부동산 거래 내역 및 아파트 정보 조회 API")
public class ApartmentController {

    private final ApartmentService apartmentService;

    @Operation(summary = "아파트별 상세 정보 조회", description = "특정 아파트의 상세 정보를 조회합니다")
    @GetMapping("/apartments/{aptSeq}")
    public ResponseEntity<CommonResponse<ApartmentDetailResponse>> getApartmentDetail(
            @PathVariable @Parameter(description = "아파트 시퀀스 (예: 11680-1)", required = true) String aptSeq,
            @Parameter(description = "평형 (예: 22)")
            @RequestParam(value = "pyung", defaultValue = "all") String pyung
    ) {
        log.info("아파트별 상세 정보 조회 요청 - aptSeq: {}, pyung: {}", aptSeq, pyung);

        try {
            ApartmentDetailResponse response = apartmentService.getApartmentDetail(aptSeq, pyung);
            log.info("아파트별 상세 정보 조회 완료 - aptSeq: {}", aptSeq);
            return ResponseEntity.ok(CommonResponse.success(response));
        } catch (IllegalArgumentException e) {
            log.error("아파트 조회 실패 - aptSeq: {}, pyung: {}, error: {}", aptSeq, pyung, e.getMessage());

            // 아파트를 찾을 수 없는 경우 404 반환
            if (e.getMessage().contains("찾을 수 없습니다")) {
                return ResponseEntity.status(404)
                    .body(CommonResponse.fail("404", e.getMessage()));
            }
            // 평형이 존재하지 않거나 유효하지 않은 경우 400 반환
            else {
                return ResponseEntity.status(400)
                    .body(CommonResponse.fail("400", e.getMessage()));
            }
        } catch (Exception e) {
            log.error("아파트 조회 중 예상치 못한 오류 발생 - aptSeq: {}, pyung: {}", aptSeq, pyung, e);
            return ResponseEntity.status(500)
                .body(CommonResponse.fail("500", "서버 내부 오류가 발생했습니다."));
        }
    }

}
