package com.ssafy.home.service;

import com.ssafy.home.dto.DongCodeDto;
import com.ssafy.home.mapper.DongCodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DongCodeService {

    private final DongCodeMapper dongCodeMapper;

    /**
     * 모든 시도 목록 조회
     */
    public List<DongCodeDto> getAllSido() {
        return dongCodeMapper.selectAllSido();
    }

    /**
     * 특정 시도의 구군 목록 조회
     */
    public List<DongCodeDto> getGugunBySido(String sidoName) {
        return dongCodeMapper.selectGugunBySido(sidoName);
    }

    /**
     * 특정 시도, 구군의 동 목록 조회
     */
    public List<DongCodeDto> getDongByGugun(String sidoName, String gugunName) {
        return dongCodeMapper.selectDongByGugun(sidoName, gugunName);
    }
}

