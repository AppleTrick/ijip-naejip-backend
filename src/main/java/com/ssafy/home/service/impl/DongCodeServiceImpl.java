package com.ssafy.home.service.impl;

import com.ssafy.home.dto.DongCodeResponse;
import com.ssafy.home.repository.DongCodeRepository;
import com.ssafy.home.service.DongCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DongCodeServiceImpl implements DongCodeService {

    private final DongCodeRepository dongCodeRepository;

    @Override
    public List<DongCodeResponse> getAllSido() {
        return dongCodeRepository.findAllSido();
    }

    @Override
    public List<DongCodeResponse> getGugunBySido(String sidoName) {
        return dongCodeRepository.findGugunBySido(sidoName);
    }

    @Override
    public List<DongCodeResponse> getDongByGugun(String sidoName, String gugunName) {
        return dongCodeRepository.findDongByGugun(sidoName, gugunName);
    }

    @Override
    public DongCodeResponse getDetail(String code) {
        return dongCodeRepository.findByCode(code);
    }
}

