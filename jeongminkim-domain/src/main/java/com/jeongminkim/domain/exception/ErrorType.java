package com.jeongminkim.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 도메인 에러 타입
 * HTTP Status 같은 인프라 의존성 제거
 */
@Getter
@RequiredArgsConstructor
public enum ErrorType {

    // 계좌 관련 에러
    ACCOUNT_NOT_FOUND("ACC001", "계좌를 찾을 수 없습니다"),
    DUPLICATE_ACCOUNT("ACC002", "이미 존재하는 계좌번호입니다"),

    // 거래 관련 에러
    INVALID_AMOUNT("TXN001", "유효하지 않은 금액입니다"),
    INSUFFICIENT_BALANCE("TXN002", "잔액이 부족합니다"),
    INVALID_TRANSFER("TXN003", "유효하지 않은 이체 요청입니다"),

    // 한도 관련 에러
    DAILY_WITHDRAWAL_LIMIT_EXCEEDED("LMT001", "일일 출금 한도를 초과했습니다"),
    DAILY_TRANSFER_LIMIT_EXCEEDED("LMT002", "일일 이체 한도를 초과했습니다"),

    // 입력 검증 에러
    VALIDATION_ERROR("VAL001", "입력값 검증에 실패했습니다"),
    INVALID_INPUT_VALUE("VAL002", "유효하지 않은 입력값입니다");

    private final String code;
    private final String message;
}