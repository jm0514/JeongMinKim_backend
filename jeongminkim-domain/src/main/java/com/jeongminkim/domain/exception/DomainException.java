package com.jeongminkim.domain.exception;

import lombok.Getter;

/**
 * 도메인 비즈니스 예외
 * 인프라 의존성 없음
 */
@Getter
public class DomainException extends RuntimeException {

    private final ErrorType errorType;
    private final String detailMessage;

    public DomainException(ErrorType errorType, String detailMessage) {
        super(errorType.getMessage() + (detailMessage != null ? ": " + detailMessage : ""));
        this.errorType = errorType;
        this.detailMessage = detailMessage;
    }

    public DomainException(ErrorType errorType) {
        this(errorType, null);
    }

    public String getFullMessage() {
        if (detailMessage != null) {
            return errorType.getMessage() + ": " + detailMessage;
        }
        return errorType.getMessage();
    }
}