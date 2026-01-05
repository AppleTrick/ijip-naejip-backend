package com.ssafy.home.controller;

import com.ssafy.home.dto.CommonResponse;
import com.ssafy.home.dto.FavoriteAddRequest;
import com.ssafy.home.dto.FavoriteResponse;
import com.ssafy.home.dto.User;
import com.ssafy.home.mapper.UserMapper;
import com.ssafy.home.service.FavoriteService;
import com.ssafy.home.util.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 관심 아파트 API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorites", description = "관심 아파트 API")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    @GetMapping
    @Operation(summary = "관심 아파트 목록 조회", description = "로그인한 사용자가 등록한 관심 아파트 목록을 조회합니다.")
    public ResponseEntity<CommonResponse<List<FavoriteResponse>>> getFavorites(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        List<FavoriteResponse> favorites = favoriteService.getFavorites(userId);
        return ResponseEntity.ok(CommonResponse.success(favorites));
    }

    @PostMapping
    @Operation(summary = "관심 아파트 추가", description = "새로운 관심 아파트를 등록합니다.")
    public ResponseEntity<CommonResponse<FavoriteResponse>> addFavorite(
            HttpServletRequest request,
            @RequestBody FavoriteAddRequest body
    ) {
        Long userId = getUserIdFromToken(request);
        try {
            FavoriteResponse response = favoriteService.addFavorite(userId, body);
            return ResponseEntity.ok(CommonResponse.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(CommonResponse.fail("400", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "관심 아파트 삭제 (ID)", description = "관심 아파트 ID(PK)를 이용하여 관심 목록에서 삭제합니다.")
    public ResponseEntity<CommonResponse<Void>> removeFavorite(
            HttpServletRequest request,
            @PathVariable Long id
    ) {
        Long userId = getUserIdFromToken(request);
        favoriteService.removeFavorite(userId, id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @DeleteMapping("/apt/{aptSeq}")
    @Operation(summary = "관심 아파트 삭제 (aptSeq)", description = "아파트 고유번호(aptSeq)를 이용하여 관심 목록에서 삭제합니다.")
    public ResponseEntity<CommonResponse<Void>> removeFavoriteByAptSeq(
            HttpServletRequest request,
            @PathVariable String aptSeq
    ) {
        Long userId = getUserIdFromToken(request);
        favoriteService.removeFavoriteByAptSeq(userId, aptSeq);
        return ResponseEntity.ok(CommonResponse.success(null));
    }


    @GetMapping("/check")
    @Operation(summary = "관심 등록 여부 확인", description = "특정 아파트(평수 포함)가 관심 목록에 등록되어 있는지 확인합니다.")
    public ResponseEntity<CommonResponse<Boolean>> checkFavorite(
            HttpServletRequest request,
            @RequestParam String aptSeq,
            @RequestParam Integer pyung
    ) {
        Long userId = getUserIdFromToken(request);
        boolean isFavorite = favoriteService.isFavorite(userId, aptSeq, pyung);
        return ResponseEntity.ok(CommonResponse.success(isFavorite));
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
