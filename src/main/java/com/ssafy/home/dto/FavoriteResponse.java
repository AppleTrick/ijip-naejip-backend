package com.ssafy.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * 관심 아파트 응답 DTO
 */
@Schema(description = "관심 아파트 정보")
@Builder
public record FavoriteResponse(
    @Schema(description = "관심 목록 ID")
    Long id,
    
    @Schema(description = "아파트 고유번호")
    String aptSeq,
    
    @Schema(description = "아파트명")
    String aptName,
    
    @Schema(description = "주소")
    String address,
    
    @Schema(description = "평수")
    Integer pyung,
    
    @Schema(description = "거래가격")
    String dealAmount,
    
    @Schema(description = "위도")
    Double latitude,
    
    @Schema(description = "경도")
    Double longitude,
    
    @Schema(description = "건축년도")
    Integer buildYear,
    
    @Schema(description = "등록일시")
    String createdAt
) {}
