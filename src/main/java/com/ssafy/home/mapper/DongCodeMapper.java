package com.ssafy.home.mapper;

import com.ssafy.home.dto.DongCodeResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DongCodeMapper {

    @Select("""
            SELECT DISTINCT dong_code, sido_name, latitude, longitude
            FROM dongcodes
            WHERE sido_name IS NOT NULL AND gugun_name IS NULL
            ORDER BY sido_name
        """)
    @ResultMap("dongCodeResultMap")
    List<DongCodeResponse> selectAllSido();

    @Select("""
            SELECT DISTINCT dong_code, sido_name, gugun_name, latitude, longitude
            FROM dongcodes
            WHERE sido_name = #{sidoName}
              AND gugun_name IS NOT NULL AND dong_name IS NULL
            ORDER BY gugun_name
        """)
    @ResultMap("dongCodeResultMap")
    List<DongCodeResponse> selectGugunBySido(@Param("sidoName") String sidoName);

    @Select("""
            SELECT dong_code, sido_name, gugun_name, dong_name, latitude, longitude
            FROM dongcodes
            WHERE sido_name = #{sidoName}
              AND gugun_name = #{gugunName}
              AND dong_name IS NOT NULL
            ORDER BY dong_name
        """)
    @ResultMap("dongCodeResultMap")
    List<DongCodeResponse> selectDongByGugun(@Param("sidoName") String sidoName,
                                             @Param("gugunName") String gugunName);

    @Select("""
            SELECT dong_code, sido_name, gugun_name, dong_name, latitude, longitude
            FROM dongcodes
            WHERE dong_code = #{code}
        """)
    @ResultMap("dongCodeResultMap")
    DongCodeResponse selectByCode(@Param("code") String code);
}
