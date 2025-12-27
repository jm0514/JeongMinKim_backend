package com.jeongminkim_backend.controller.api;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class CommonApiResponses {

    /**
     * 400 Bad Request - 잘못된 요청
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (유효성 검증 실패)",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            example = """
                                    {
                                      "success": false,
                                      "code": "VALIDATION_ERROR",
                                      "message": "계좌번호는 10~20자리여야 합니다",
                                      "data": null,
                                      "timestamp": "2024-01-15T10:30:00"
                                    }
                                    """
                    )
            )
    )
    public @interface BadRequest {
    }

    /**
     * 404 Not Found - 리소스를 찾을 수 없음
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponse(
            responseCode = "404",
            description = "계좌를 찾을 수 없음",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            example = """
                                    {
                                      "success": false,
                                      "code": "ACCOUNT_NOT_FOUND",
                                      "message": "계좌를 찾을 수 없습니다: 1234567890",
                                      "data": null,
                                      "timestamp": "2024-01-15T10:35:00"
                                    }
                                    """
                    )
            )
    )
    public @interface NotFound {
    }

    /**
     * 409 Conflict - 중복된 리소스
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponse(
            responseCode = "409",
            description = "중복된 계좌번호",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            example = """
                                    {
                                      "success": false,
                                      "code": "DUPLICATE_ACCOUNT",
                                      "message": "이미 존재하는 계좌번호입니다: 1234567890",
                                      "data": null,
                                      "timestamp": "2024-01-15T10:30:00"
                                    }
                                    """
                    )
            )
    )
    public @interface Conflict {
    }

    /**
     * 500 Internal Server Error - 서버 오류
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            example = """
                                    {
                                      "success": false,
                                      "code": "INTERNAL_SERVER_ERROR",
                                      "message": "서버 내부 오류가 발생했습니다",
                                      "data": null,
                                      "timestamp": "2024-01-15T10:30:00"
                                    }
                                    """
                    )
            )
    )
    public @interface InternalServerError {
    }
}