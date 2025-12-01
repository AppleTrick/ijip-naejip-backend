package com.ssafy.home.service;

import com.ssafy.home.dto.DongCodeResponse;

import java.util.List;

public interface DongCodeService {
    List<DongCodeResponse> getAllSido();
    List<DongCodeResponse> getGugunBySido(String sidoName);
    List<DongCodeResponse> getDongByGugun(String sidoName, String gugunName);
    DongCodeResponse getDetail(String code);
}

