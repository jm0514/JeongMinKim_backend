package com.jeongminkim.application.port.in;

/**
 * 계좌 삭제 Use Case (Inbound Port)
 */
public interface DeleteAccountUseCase {
    void deleteAccount(String accountNumber);
}
