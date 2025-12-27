package com.jeongminkim_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeongminkim_backend.dto.response.ResponseMessage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApiResponse<T> {

    private final boolean success;

    private final String code;

    private final String message;

    private final T data;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime timestamp;

    /**
     * 성공 응답 (데이터 있음, ResponseMessage 사용)
     */
    public static <T> ApiResponse<T> success(T data, ResponseMessage responseMessage) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(responseMessage.getCode())
                .message(responseMessage.getMessage())
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 성공 응답 (데이터 없음, ResponseMessage 사용)
     */
    public static <T> ApiResponse<T> success(ResponseMessage responseMessage) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(responseMessage.getCode())
                .message(responseMessage.getMessage())
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 실패 응답
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
