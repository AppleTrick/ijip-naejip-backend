package com.ssafy.home.mapper;

import com.ssafy.home.dto.FavoriteApartment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 관심 아파트 Mapper
 */
@Mapper
public interface FavoriteMapper {
    
    /**
     * 사용자의 관심 아파트 목록 조회
     */
    List<FavoriteApartment> findByUserId(@Param("userId") Long userId);
    
    /**
     * 관심 아파트 추가
     */
    void insert(FavoriteApartment favorite);
    
    /**
     * 관심 아파트 삭제 (ID)
     */
    void deleteById(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 관심 아파트 삭제 (aptSeq)
     */
    void deleteByAptSeq(@Param("userId") Long userId, @Param("aptSeq") String aptSeq);

    
    /**
     * 중복 확인 (동일 아파트 + 평수)
     */
    boolean existsByUserIdAndAptSeqAndPyung(
        @Param("userId") Long userId, 
        @Param("aptSeq") String aptSeq, 
        @Param("pyung") Integer pyung
    );
    
    /**
     * 사용자별 관심 아파트 개수
     */
    int countByUserId(@Param("userId") Long userId);
}
