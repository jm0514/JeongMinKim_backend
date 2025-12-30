package com.jeongminkim.application.port.in;

import com.jeongminkim.domain.model.Transaction;

import java.math.BigDecimal;

/**
 * 입금 Use Case (Inbound Port)
 */
public interface DepositUseCase {
    Transaction deposit(String accountNumber, BigDecimal amount);
}