package com.ssafy.home.dto;

import lombok.Data;

@Data
public class HouseDealDto {
    private String dongName;
    private String gugunName;
    private String sidoName;
    private String dongCode;
    // 조인된 dongcode 정보

    private String longitude;
    private String latitude;
    private Integer buildYear;
    private String jibun;
    private String roadNm;
    private String aptNm;
    // 조인된 houseinfo 정보

    private Integer dealAmount;
    private Double excluUseAr;
    private Integer dealDate;
    private String floor;
    private String aptDong;
    private String aptSeq;
    private Integer no;
}
