package com.jeongminkim_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeongminkim_backend.dto.response.ResponseMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "공통 응답 형식")
public class CommonResponse<T> {

    @Schema(description = "성공 여부", example = "true")
    private final boolean success;

    @Schema(description = "응답 코드", example = "ACCOUNT_CREATED")
    private final String code;

    @Schema(description = "응답 메시지", example = "계좌가 성공적으로 생성되었습니다")
    private final String message;

    @Schema(description = "응답 데이터")
    private final T data;

    @Schema(description = "응답 시간", example = "2024-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime timestamp;

    /**
     * 성공 응답 (데이터 있음, ResponseMessage 사용)
     */
    public static <T> CommonResponse<T> success(T data, ResponseMessage responseMessage) {
        return CommonResponse.<T>builder()
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
    public static <T> CommonResponse<T> success(ResponseMessage responseMessage) {
        return CommonResponse.<T>builder()
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
    public static <T> CommonResponse<T> error(String code, String message) {
        return CommonResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }
}