package com.ssafy.home.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SqlResultWrapper {
    private String generatedSql;
    private List<Map<String, Object>> resultTable;
    private int rowCount;
    private boolean isSuccess;
    private String errorMessage;
}
