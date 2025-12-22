package com.ssafy.home.service;

import com.ssafy.home.dto.FavoriteAddRequest;
import com.ssafy.home.dto.FavoriteResponse;

import java.util.List;

/**
 * 관심 아파트 서비스 인터페이스
 */
public interface FavoriteService {
    
    /**
     * 사용자의 관심 아파트 목록 조회
     */
    List<FavoriteResponse> getFavorites(Long userId);
    
    /**
     * 관심 아파트 추가
     */
    FavoriteResponse addFavorite(Long userId, FavoriteAddRequest request);
    
    /**
     * 관심 아파트 삭제
     */
    void removeFavorite(Long userId, Long favoriteId);
    
    /**
     * 중복 확인
     */
    boolean isFavorite(Long userId, String aptSeq, Integer pyung);
}
