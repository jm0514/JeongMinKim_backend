package com.jeongminkim.core.repository;

import com.jeongminkim.core.domain.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * 계좌번호로 계좌 조회
     */
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber AND a.deletedAt IS NULL")
    Optional<Account> findByAccountNumber(@Param("accountNumber") String accountNumber);

    /**
     * 계좌번호로 계좌 조회 (비관적 락)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber AND a.deletedAt IS NULL")
    Optional<Account> findByAccountNumberWithLock(@Param("accountNumber") String accountNumber);

    /**
     * 계좌번호 존재 여부 확인
     */
    boolean existsByAccountNumberAndDeletedAtIsNull(String accountNumber);
}
