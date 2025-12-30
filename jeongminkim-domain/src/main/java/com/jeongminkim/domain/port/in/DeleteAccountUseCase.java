package com.jeongminkim.domain.port.in;

/**
 * 계좌 삭제 Use Case (Inbound Port)
 */
public interface DeleteAccountUseCase {
    void deleteAccount(String accountNumber);
}