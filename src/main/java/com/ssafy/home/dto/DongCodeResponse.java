package com.ssafy.home.dto;

import lombok.Data;

@Data
public class DongCodeResponse {
    private String dongCode;
    private String sidoName;
    private String gugunName;
    private String dongName;

    private Double latitude;
    private Double longitude;
}

