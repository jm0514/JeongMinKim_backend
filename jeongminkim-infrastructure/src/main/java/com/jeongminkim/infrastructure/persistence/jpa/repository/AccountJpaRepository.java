package com.jeongminkim.infrastructure.persistence.jpa.repository;

import com.jeongminkim.infrastructure.persistence.jpa.entity.AccountEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Account JPA Repository
 */
public interface AccountJpaRepository extends JpaRepository<AccountEntity, Long> {

    @Query("SELECT a FROM AccountEntity a WHERE a.accountNumber = :accountNumber AND a.deletedAt IS NULL")
    Optional<AccountEntity> findByAccountNumber(@Param("accountNumber") String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AccountEntity a WHERE a.accountNumber = :accountNumber AND a.deletedAt IS NULL")
    Optional<AccountEntity> findByAccountNumberWithLock(@Param("accountNumber") String accountNumber);

    boolean existsByAccountNumberAndDeletedAtIsNull(String accountNumber);
}