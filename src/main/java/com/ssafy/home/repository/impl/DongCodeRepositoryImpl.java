package com.ssafy.home.repository.impl;

import com.ssafy.home.dto.DongCodeResponse;
import com.ssafy.home.mapper.DongCodeMapper;
import com.ssafy.home.repository.DongCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DongCodeRepositoryImpl implements DongCodeRepository {

    private final DongCodeMapper dongCodeMapper;

    @Override
    public List<DongCodeResponse> findAllSido() {
        return dongCodeMapper.selectAllSido();
    }

    @Override
    public List<DongCodeResponse> findGugunBySido(String sidoName) {
        return dongCodeMapper.selectGugunBySido(sidoName);
    }

    @Override
    public List<DongCodeResponse> findDongByGugun(String sidoName, String gugunName) {
        return dongCodeMapper.selectDongByGugun(sidoName, gugunName);
    }

    @Override
    public DongCodeResponse findByCode(String code) {
        return dongCodeMapper.selectByCode(code);
    }
}

