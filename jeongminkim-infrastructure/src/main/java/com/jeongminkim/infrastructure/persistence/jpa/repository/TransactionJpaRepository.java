package com.jeongminkim.infrastructure.persistence.jpa.repository;

import com.jeongminkim.domain.model.TransactionType;
import com.jeongminkim.infrastructure.persistence.jpa.entity.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction JPA Repository
 */
public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, Long> {

    Page<TransactionEntity> findAllByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);

    long countByAccountId(Long accountId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t " +
            "WHERE t.accountId = :accountId " +
            "AND t.transactionType = :transactionType " +
            "AND t.createdAt >= :startDateTime " +
            "AND t.createdAt < :endDateTime")
    BigDecimal sumAmountByAccountIdAndTypeAndDateRange(
            @Param("accountId") Long accountId,
            @Param("transactionType") TransactionType transactionType,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );
}