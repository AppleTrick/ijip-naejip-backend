package com.ssafy.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 관심 아파트 추가 요청 DTO
 */
@Schema(description = "관심 아파트 추가 요청")
public record FavoriteAddRequest(
    @Schema(description = "아파트 고유번호", example = "11680-102")
    String aptSeq,
    
    @Schema(description = "아파트명", example = "래미안 강남")
    String aptName,
    
    @Schema(description = "주소", example = "서울특별시 강남구 역삼동")
    String address,
    
    @Schema(description = "선택 평수", example = "34")
    Integer pyung,
    
    @Schema(description = "거래가격", example = "15억 5000만원")
    String dealAmount
) {}
