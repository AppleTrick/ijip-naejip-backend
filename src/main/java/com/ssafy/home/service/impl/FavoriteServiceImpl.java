package com.ssafy.home.service.impl;

import com.ssafy.home.dto.FavoriteAddRequest;
import com.ssafy.home.dto.FavoriteApartment;
import com.ssafy.home.dto.FavoriteResponse;
import com.ssafy.home.mapper.FavoriteMapper;
import com.ssafy.home.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 관심 아파트 서비스 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private static final int MAX_FAVORITES = 20;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public List<FavoriteResponse> getFavorites(Long userId) {
        log.info("관심 아파트 목록 조회 - userId: {}", userId);
        return favoriteMapper.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public FavoriteResponse addFavorite(Long userId, FavoriteAddRequest request) {
        log.info("관심 아파트 추가 - userId: {}, aptSeq: {}, pyung: {}", userId, request.aptSeq(), request.pyung());
        
        // 중복 확인
        if (favoriteMapper.existsByUserIdAndAptSeqAndPyung(userId, request.aptSeq(), request.pyung())) {
            throw new IllegalArgumentException("이미 등록된 관심 아파트입니다.");
        }
        
        // 최대 개수 확인
        int count = favoriteMapper.countByUserId(userId);
        if (count >= MAX_FAVORITES) {
            throw new IllegalArgumentException("관심 아파트는 최대 " + MAX_FAVORITES + "개까지 등록 가능합니다.");
        }
        
        FavoriteApartment favorite = FavoriteApartment.builder()
                .userId(userId)
                .aptSeq(request.aptSeq())
                .aptName(request.aptName())
                .address(request.address())
                .pyung(request.pyung())
                .dealAmount(request.dealAmount())
                .build();
        
        favoriteMapper.insert(favorite);
        log.info("관심 아파트 추가 완료 - id: {}", favorite.getId());
        
        return toResponse(favorite);
    }

    @Override
    public void removeFavorite(Long userId, Long favoriteId) {
        log.info("관심 아파트 삭제 - userId: {}, favoriteId: {}", userId, favoriteId);
        favoriteMapper.deleteById(favoriteId, userId);
    }

    @Override
    public boolean isFavorite(Long userId, String aptSeq, Integer pyung) {
        return favoriteMapper.existsByUserIdAndAptSeqAndPyung(userId, aptSeq, pyung);
    }

    private FavoriteResponse toResponse(FavoriteApartment fav) {
        return FavoriteResponse.builder()
                .id(fav.getId())
                .aptSeq(fav.getAptSeq())
                .aptName(fav.getAptName())
                .address(fav.getAddress())
                .pyung(fav.getPyung())
                .dealAmount(fav.getDealAmount())
                .createdAt(fav.getCreatedAt() != null ? fav.getCreatedAt().format(DATE_FORMATTER) : null)
                .build();
    }
}
