package com.ssafy.home.ai.mapper;

import com.ssafy.home.ai.dto.AIReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AIReportMapper {

    /**
     * 보고서 저장
     */
    void insertReport(AIReport report);

    /**
     * 사용자별 보고서 목록 조회 (최신순)
     */
    List<AIReport> findByUserId(@Param("userId") Long userId);

    /**
     * 보고서 상세 조회
     */
    AIReport findById(@Param("id") Long id);

    /**
     * 보고서 삭제
     */
    void deleteById(@Param("id") Long id);

    /**
     * 사용자의 보고서인지 확인
     */
    boolean existsByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}

