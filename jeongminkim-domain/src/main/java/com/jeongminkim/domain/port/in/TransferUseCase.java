package com.jeongminkim.domain.port.in;

import com.jeongminkim.domain.model.Transaction;

import java.math.BigDecimal;

/**
 * 이체 Use Case (Inbound Port)
 */
public interface TransferUseCase {

    /**
     * 계좌 간 이체 수행
     * @return TransferResult (출금 및 입금 거래 정보 포함)
     */
    TransferResult transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount);

    /**
     * 이체 결과
     */
    record TransferResult(Transaction fromTransaction, Transaction toTransaction) {}
}