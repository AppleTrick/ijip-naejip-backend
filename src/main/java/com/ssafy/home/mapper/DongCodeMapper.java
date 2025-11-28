package com.ssafy.home.mapper;

import com.ssafy.home.dto.DongCodeDto;
import com.ssafy.home.dto.AddressCoordinateDto;
import com.ssafy.home.dto.AreaScope;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DongCodeMapper {
    /**
     * 모든 시도 목록 조회
     */
    List<DongCodeDto> selectAllSido();

    /**
     * 특정 시도의 구군 목록 조회
     */
    List<DongCodeDto> selectGugunBySido(@Param("sidoName") String sidoName);

    /**
     * 특정 시도, 구군의 동 목록 조회
     */
    List<DongCodeDto> selectDongByGugun(@Param("sidoName") String sidoName,
                                        @Param("gugunName") String gugunName);

    /**
     * 법정동 코드의 대표 좌표 조회
     */
    AddressCoordinateDto selectCoordinateByCode(@Param("code") String code);
}
