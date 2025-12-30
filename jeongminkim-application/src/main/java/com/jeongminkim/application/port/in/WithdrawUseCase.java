package com.jeongminkim.application.port.in;

import com.jeongminkim.domain.model.Transaction;

import java.math.BigDecimal;

/**
 * 출금 Use Case (Inbound Port)
 */
public interface WithdrawUseCase {
    Transaction withdraw(String accountNumber, BigDecimal amount);
}