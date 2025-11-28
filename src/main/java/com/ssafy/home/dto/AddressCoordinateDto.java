package com.ssafy.home.dto;

import lombok.Data;

@Data
public class AddressCoordinateDto {
    private String dongCode; // full 10-digit legal dong code
    private String sidoName;
    private String gugunName;
    private String dongName;

    private String latitude;
    private String longitude;
}

