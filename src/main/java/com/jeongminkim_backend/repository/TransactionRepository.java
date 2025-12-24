package com.jeongminkim_backend.repository;

import com.jeongminkim_backend.domain.entity.Transaction;
import com.jeongminkim_backend.domain.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * 계좌 ID로 거래 내역 조회 (페이징, 최신순)
     */
    Page<Transaction> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);

    /**
     * 특정 계좌의 특정 거래 유형에 대한 당일 거래 금액 합계 조회
     * @param accountId 계좌 ID
     * @param transactionType 거래 유형
     * @param startOfDay 오늘 시작 시간
     * @param endOfDay 오늘 종료 시간
     * @return 거래 금액 합계
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.accountId = :accountId " +
           "AND t.transactionType = :transactionType " +
           "AND t.createdAt BETWEEN :startOfDay AND :endOfDay")
    BigDecimal sumAmountByAccountIdAndTypeAndDateRange(
            @Param("accountId") Long accountId,
            @Param("transactionType") TransactionType transactionType,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );
}
