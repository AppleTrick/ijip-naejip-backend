package com.ssafy.home.dto;

public record CommonResponse<T>(
        String code,
        String message,
        T data
) {
    // 성공 응답 (데이터 포함)
    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>("200", "Success", data);
    }

    // 성공 응답 (커스텀 메시지)
    public static <T> CommonResponse<T> success(String message, T data) {
        return new CommonResponse<>("200", message, data);
    }

    // 실패 응답
    public static <T> CommonResponse<T> fail(String code, String message) {
        return new CommonResponse<>(code, message, null);
    }

    // 실패 응답 (데이터 포함)
    public static <T> CommonResponse<T> fail(String code, String message, T data) {
        return new CommonResponse<>(code, message, data);
    }
}
