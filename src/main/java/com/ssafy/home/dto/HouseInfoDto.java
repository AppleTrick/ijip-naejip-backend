package com.ssafy.home.dto;

import lombok.Data;

@Data
public class HouseInfoDto {
    private String aptSeq;
    private String sggCd;
    private String umdCd;
    private String jibun;
    private String roadNm;
    private String roadNmBonbun;
    private String roadNmBubun;
    private String aptNm;
    private Integer buildYear;
    private String latitude;
    private String longitude;
    private String geoStatus;
}

