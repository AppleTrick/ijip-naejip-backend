package com.ssafy.home.mapper;

import com.ssafy.home.dto.DongCodeResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DongCodeMapper {

    List<DongCodeResponse> selectAllSido();
    List<DongCodeResponse> selectGugunBySido(@Param("sidoName") String sidoName);
    List<DongCodeResponse> selectDongByGugun(@Param("sidoName") String sidoName,
                                             @Param("gugunName") String gugunName);
    DongCodeResponse selectByCode(@Param("code") String code);
}
