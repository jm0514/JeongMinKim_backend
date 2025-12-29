package com.jeongminkim.core.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String detailMessage;

    public BusinessException(ErrorCode errorCode, String detailMessage) {
        super(errorCode.getMessage() + ": " + detailMessage);
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
    }

    public String getFullMessage() {
        if (detailMessage != null) {
            return errorCode.getMessage() + ": " + detailMessage;
        }
        return errorCode.getMessage();
    }
}
