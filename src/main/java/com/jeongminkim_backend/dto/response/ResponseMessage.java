package com.jeongminkim_backend.dto.response;

import lombok.Getter;

@Getter
public enum ResponseMessage {

    // 기본 응답
    SUCCESS("SUC000", "요청이 성공적으로 처리되었습니다."),

    // 계좌 관련
    ACCOUNT_CREATED("ACC_S001", "계좌가 성공적으로 생성되었습니다."),
    ACCOUNT_RETRIEVED("ACC_S002", "계좌 조회가 완료되었습니다."),
    ACCOUNT_DELETED("ACC_S003", "계좌가 성공적으로 삭제되었습니다."),

    // 거래 관련
    DEPOSIT_SUCCESS("TXN_S001", "입금이 완료되었습니다."),
    WITHDRAWAL_SUCCESS("TXN_S002", "출금이 완료되었습니다."),
    TRANSFER_SUCCESS("TXN_S003", "이체가 완료되었습니다."),
    TRANSACTION_HISTORY_RETRIEVED("TXN_S004", "거래내역 조회가 완료되었습니다.");

    private final String code;
    private final String message;

    ResponseMessage(String code, String message) {
        this.code = code;
        this.message = message;
    }
}