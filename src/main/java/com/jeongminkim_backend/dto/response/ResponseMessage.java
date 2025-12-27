package com.jeongminkim_backend.dto.response;

import lombok.Getter;

@Getter
public enum ResponseMessage {

    // 기본 응답
    SUCCESS("요청이 성공적으로 처리되었습니다."),

    // 계좌 관련
    ACCOUNT_CREATED("계좌가 성공적으로 생성되었습니다."),
    ACCOUNT_RETRIEVED("계좌 조회가 완료되었습니다."),
    ACCOUNT_DELETED("계좌가 성공적으로 삭제되었습니다."),

    // 거래 관련
    DEPOSIT_SUCCESS("입금이 완료되었습니다."),
    WITHDRAWAL_SUCCESS("출금이 완료되었습니다."),
    TRANSFER_SUCCESS("이체가 완료되었습니다."),
    TRANSACTION_HISTORY_RETRIEVED("거래내역 조회가 완료되었습니다.");

    private final String message;

    ResponseMessage(String message) {
        this.message = message;
    }
}