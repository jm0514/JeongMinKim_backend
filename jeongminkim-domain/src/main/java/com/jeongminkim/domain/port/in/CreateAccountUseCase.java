package com.jeongminkim.domain.port.in;

import com.jeongminkim.domain.model.Account;

/**
 * 계좌 생성 Use Case (Inbound Port)
 */
public interface CreateAccountUseCase {
    Account createAccount(String accountNumber, String ownerName);
}