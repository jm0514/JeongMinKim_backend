package com.jeongminkim.domain.port.out;

import com.jeongminkim.domain.model.Account;

import java.util.Optional;

/**
 * 계좌 영속성 포트 (Outbound Port)
 * Infrastructure 계층에서 구현
 */
public interface AccountPort {

    /**
     * 계좌 저장
     */
    Account save(Account account);

    /**
     * 계좌번호로 계좌 조회
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * 계좌번호로 계좌 조회 (락)
     */
    Optional<Account> findByAccountNumberWithLock(String accountNumber);

    /**
     * 계좌 존재 여부 확인
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     * 계좌 삭제
     */
    void delete(Account account);
}