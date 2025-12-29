package com.jeongminkim_backend.service;

import com.jeongminkim_backend.domain.entity.Account;
import com.jeongminkim_backend.exception.BusinessException;
import com.jeongminkim_backend.exception.ErrorCode;
import com.jeongminkim_backend.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountReader {

    private final AccountRepository accountRepository;

    /**
     * 계좌 조회 (락 없음)
     */
    public Account findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, accountNumber));
    }

    /**
     * 계좌 조회 (비관적 락)
     */
    public Account findByAccountNumberWithLock(String accountNumber) {
        return accountRepository.findByAccountNumberWithLock(accountNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, accountNumber));
    }
}
