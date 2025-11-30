package com.ssafy.home.mapper;

import com.ssafy.home.dto.DongCodeResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DongCodeMapper {
    /**
     * 모든 시도 목록 조회
     */
    List<DongCodeResponse> selectAllSido();

    /**
     * 특정 시도의 구군 목록 조회
     */
    List<DongCodeResponse> selectGugunBySido(@Param("sidoName") String sidoName);

    /**
     * 특정 시도, 구군의 동 목록 조회
     */
    List<DongCodeResponse> selectDongByGugun(@Param("sidoName") String sidoName,
                                             @Param("gugunName") String gugunName);

    /**
     * 법정동 코드 조회
     */
    DongCodeResponse selectByCode(@Param("code") String code);
}
