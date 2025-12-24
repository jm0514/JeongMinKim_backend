package com.jeongminkim_backend.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 계좌 관련 에러
    ACCOUNT_NOT_FOUND("ACC001", HttpStatus.NOT_FOUND, "계좌를 찾을 수 없습니다"),
    DUPLICATE_ACCOUNT("ACC002", HttpStatus.CONFLICT, "이미 존재하는 계좌번호입니다"),

    // 거래 관련 에러
    INVALID_AMOUNT("TXN001", HttpStatus.BAD_REQUEST, "유효하지 않은 금액입니다"),
    INSUFFICIENT_BALANCE("TXN002", HttpStatus.BAD_REQUEST, "잔액이 부족합니다"),

    // 한도 관련 에러
    DAILY_WITHDRAWAL_LIMIT_EXCEEDED("LMT001", HttpStatus.BAD_REQUEST, "일일 출금 한도를 초과했습니다"),
    DAILY_TRANSFER_LIMIT_EXCEEDED("LMT002", HttpStatus.BAD_REQUEST, "일일 이체 한도를 초과했습니다"),

    // 입력 검증 에러
    VALIDATION_ERROR("VAL001", HttpStatus.BAD_REQUEST, "입력값 검증에 실패했습니다"),
    INVALID_INPUT_VALUE("VAL002", HttpStatus.BAD_REQUEST, "유효하지 않은 입력값입니다"),

    // 시스템 에러
    INTERNAL_SERVER_ERROR("SYS001", HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류가 발생했습니다");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
