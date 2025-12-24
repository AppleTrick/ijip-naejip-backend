package com.ssafy.home.ai.controller;

import com.ssafy.home.ai.dto.AIReportResponse;
import com.ssafy.home.ai.service.AIReportService;
import com.ssafy.home.dto.CommonResponse;
import com.ssafy.home.dto.User;
import com.ssafy.home.mapper.UserMapper;
import com.ssafy.home.util.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 보고서 조회 및 관리 API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ai/reports")
@RequiredArgsConstructor
@Tag(name = "AI Reports", description = "AI 분석 보고서 조회 및 관리 API")
public class AIReportController {

    private final AIReportService aiReportService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    @GetMapping
    @Operation(summary = "보고서 목록 조회", description = "로그인한 사용자의 모든 보고서 목록을 최신순으로 조회합니다.")
    public ResponseEntity<CommonResponse<List<AIReportResponse>>> getReports(
            HttpServletRequest request
    ) {
        try {
            Long userId = getUserIdFromToken(request);
            List<AIReportResponse> reports = aiReportService.getReportsByUserId(userId);
            return ResponseEntity.ok(CommonResponse.success(reports));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401)
                    .body(CommonResponse.fail("401", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "보고서 상세 조회", description = "특정 보고서의 상세 내용을 조회합니다.")
    public ResponseEntity<CommonResponse<AIReportResponse>> getReport(
            HttpServletRequest request,
            @PathVariable Long id
    ) {
        try {
            Long userId = getUserIdFromToken(request);
            AIReportResponse report = aiReportService.getReportById(userId, id);
            return ResponseEntity.ok(CommonResponse.success(report));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401)
                    .body(CommonResponse.fail("401", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                    .body(CommonResponse.fail("404", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "보고서 삭제", description = "특정 보고서를 삭제합니다. 본인의 보고서만 삭제 가능합니다.")
    public ResponseEntity<CommonResponse<Void>> deleteReport(
            HttpServletRequest request,
            @PathVariable Long id
    ) {
        try {
            Long userId = getUserIdFromToken(request);
            aiReportService.deleteReport(userId, id);
            return ResponseEntity.ok(CommonResponse.success(null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403)
                    .body(CommonResponse.fail("403", e.getMessage()));
        }
    }

    private Long getUserIdFromToken(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        String email = jwtTokenProvider.getEmail(token);
        User user = userMapper.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));
        return user.getId();
    }
}

