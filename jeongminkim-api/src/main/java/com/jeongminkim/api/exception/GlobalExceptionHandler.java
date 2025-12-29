package com.jeongminkim.api.exception;

import com.jeongminkim.application.dto.CommonResponse;
import com.jeongminkim.application.dto.CommonResponseFactory;
import com.jeongminkim.core.exception.BusinessException;
import com.jeongminkim.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final CommonResponseFactory commonResponseFactory;

    /**
     * BusinessException 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CommonResponse<Void>> handleBusinessException(BusinessException ex) {
        log.error("BusinessException: {}", ex.getMessage());

        ErrorCode errorCode = ex.getErrorCode();
        CommonResponse<Void> response = commonResponseFactory.error(
                errorCode.getCode(),
                ex.getFullMessage()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    /**
     * {@code @Valid} 애노테이션 검증 실패 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException: {}", ex.getMessage());

        String validationMessages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        CommonResponse<Void> response = commonResponseFactory.error(
                errorCode.getCode(),
                validationMessages
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("IllegalArgumentException: {}", ex.getMessage());

        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        CommonResponse<Void> response = commonResponseFactory.error(
                errorCode.getCode(),
                ex.getMessage()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    /**
     * 기타 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleException(Exception ex) {
        log.error("Unexpected Exception: ", ex);

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        CommonResponse<Void> response = commonResponseFactory.error(
                errorCode.getCode(),
                errorCode.getMessage()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }
}
