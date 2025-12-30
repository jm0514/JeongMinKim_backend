package com.jeongminkim.application.port.in;

import com.jeongminkim.domain.model.Account;

/**
 * 계좌 조회 Use Case (Inbound Port)
 */
public interface GetAccountUseCase {
    Account getAccount(String accountNumber);
}
