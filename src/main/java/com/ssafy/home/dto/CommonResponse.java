package com.ssafy.home.dto;

public record CommonResponse<T>(
        String code,
        String message,
        T data
) {}