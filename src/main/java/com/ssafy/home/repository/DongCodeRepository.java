package com.ssafy.home.repository;

import com.ssafy.home.dto.DongCodeResponse;

import java.util.List;


public interface DongCodeRepository {
    List<DongCodeResponse> findAllSido();
    List<DongCodeResponse> findGugunBySido(String sidoName);
    List<DongCodeResponse> findDongByGugun(String sidoName, String gugunName);
    DongCodeResponse findByCode(String code);
}

