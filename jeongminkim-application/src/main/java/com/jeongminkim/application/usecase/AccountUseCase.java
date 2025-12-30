package com.jeongminkim.application.usecase;

import com.jeongminkim.domain.exception.DomainException;
import com.jeongminkim.domain.exception.ErrorType;
import com.jeongminkim.domain.model.Account;
import com.jeongminkim.domain.port.in.CreateAccountUseCase;
import com.jeongminkim.domain.port.in.DeleteAccountUseCase;
import com.jeongminkim.domain.port.in.GetAccountUseCase;
import com.jeongminkim.domain.port.out.AccountPort;
import com.jeongminkim.domain.port.out.TimePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 계좌 Use Case 구현
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountUseCase implements CreateAccountUseCase, DeleteAccountUseCase, GetAccountUseCase {

    private final AccountPort accountPort;
    private final TimePort timePort;

    @Override
    @Transactional
    public Account createAccount(String accountNumber, String ownerName) {
        // 계좌 중복 확인
        if (accountPort.existsByAccountNumber(accountNumber)) {
            throw new DomainException(ErrorType.DUPLICATE_ACCOUNT, accountNumber);
        }

        // 계좌 생성
        Account account = Account.create(accountNumber, ownerName);

        // 저장
        return accountPort.save(account);
    }

    @Override
    @Transactional
    public void deleteAccount(String accountNumber) {
        // 계좌 조회
        Account account = accountPort.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new DomainException(ErrorType.ACCOUNT_NOT_FOUND, accountNumber));

        // Soft Delete
        Account deletedAccount = account.delete(timePort.now());

        // 저장
        accountPort.save(deletedAccount);
    }

    @Override
    public Account getAccount(String accountNumber) {
        return accountPort.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new DomainException(ErrorType.ACCOUNT_NOT_FOUND, accountNumber));
    }
}