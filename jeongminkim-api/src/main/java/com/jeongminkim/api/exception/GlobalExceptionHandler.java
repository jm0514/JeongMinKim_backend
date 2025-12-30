package com.jeongminkim.api.exception;

import com.jeongminkim.application.dto.CommonResponse;
import com.jeongminkim.application.dto.CommonResponseFactory;
import com.jeongminkim.domain.exception.DomainException;
import com.jeongminkim.domain.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
     * DomainException 처리
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<CommonResponse<Void>> handleDomainException(DomainException ex) {
        log.error("DomainException: {}", ex.getMessage());

        ErrorType errorType = ex.getErrorType();
        HttpStatus status = mapErrorTypeToHttpStatus(errorType);

        CommonResponse<Void> response = commonResponseFactory.error(
                errorType.getCode(),
                ex.getFullMessage()
        );

        return ResponseEntity
                .status(status)
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

        CommonResponse<Void> response = commonResponseFactory.error(
                ErrorType.VALIDATION_ERROR.getCode(),
                validationMessages
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("IllegalArgumentException: {}", ex.getMessage());

        CommonResponse<Void> response = commonResponseFactory.error(
                ErrorType.INVALID_INPUT_VALUE.getCode(),
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * 기타 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleException(Exception ex) {
        log.error("Unexpected Exception: ", ex);

        CommonResponse<Void> response = commonResponseFactory.error(
                "SYS001",
                "내부 서버 오류가 발생했습니다"
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    /**
     * ErrorType을 HttpStatus로 매핑
     */
    private HttpStatus mapErrorTypeToHttpStatus(ErrorType errorType) {
        return switch (errorType) {
            case ACCOUNT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case DUPLICATE_ACCOUNT -> HttpStatus.CONFLICT;
            case INVALID_AMOUNT,
                 INSUFFICIENT_BALANCE,
                 INVALID_TRANSFER,
                 DAILY_WITHDRAWAL_LIMIT_EXCEEDED,
                 DAILY_TRANSFER_LIMIT_EXCEEDED,
                 VALIDATION_ERROR,
                 INVALID_INPUT_VALUE -> HttpStatus.BAD_REQUEST;
        };
    }
}
