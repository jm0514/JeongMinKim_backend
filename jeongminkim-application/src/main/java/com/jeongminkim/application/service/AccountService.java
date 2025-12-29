package com.jeongminkim.application.service;

import com.jeongminkim.application.common.time.TimeProvider;
import com.jeongminkim.core.domain.entity.Account;
import com.jeongminkim.application.dto.request.CreateAccountRequest;
import com.jeongminkim.application.dto.response.AccountResponse;
import com.jeongminkim.core.exception.BusinessException;
import com.jeongminkim.core.exception.ErrorCode;
import com.jeongminkim.core.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountReader accountReader;
    private final TimeProvider timeProvider;

    /**
     * 계좌 생성
     */
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        log.info("계좌 생성 시작: {}", request.getAccountNumber());

        if (accountRepository.existsByAccountNumberAndDeletedAtIsNull(request.getAccountNumber())) {
            throw new BusinessException(ErrorCode.DUPLICATE_ACCOUNT, request.getAccountNumber());
        }

        Account account = Account.create(request.getAccountNumber(), request.getOwnerName());
        Account savedAccount = accountRepository.save(account);

        log.info("계좌 생성 완료: {}", savedAccount.getAccountNumber());
        return AccountResponse.from(savedAccount);
    }

    /**
     * 계좌 조회
     */
    public AccountResponse getAccount(String accountNumber) {
        log.info("계좌 조회: {}", accountNumber);

        Account account = accountReader.findByAccountNumber(accountNumber);

        return AccountResponse.from(account);
    }

    /**
     * 계좌 삭제
     */
    @Transactional
    public void deleteAccount(String accountNumber) {
        log.info("계좌 삭제 시작: {}", accountNumber);

        Account account = accountReader.findByAccountNumber(accountNumber);

        account.delete(timeProvider.now());
        log.info("계좌 삭제 완료 (Soft Delete): {}", accountNumber);
    }
}
