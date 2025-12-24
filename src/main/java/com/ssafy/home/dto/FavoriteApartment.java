package com.ssafy.home.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 관심 아파트 Entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteApartment {
    private Long id;
    private Long userId;
    private String aptSeq;
    private String aptName;
    private String address;
    private Integer pyung;
    private String dealAmount;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
}
