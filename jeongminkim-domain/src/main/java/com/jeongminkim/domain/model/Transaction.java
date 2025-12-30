package com.jeongminkim.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 순수 도메인 모델 - 인프라 의존성 없음
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Transaction {

    private Long id;
    private Long accountId;
    private TransactionType transactionType;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal balanceAfter;
    private String relatedAccountNumber;
    private LocalDateTime createdAt;

    /**
     * 거래 내역 생성 팩토리 메서드
     */
    public static Transaction create(
            Long accountId,
            TransactionType transactionType,
            BigDecimal amount,
            BigDecimal fee,
            BigDecimal balanceAfter,
            String relatedAccountNumber,
            LocalDateTime createdAt
    ) {
        return Transaction.builder()
                .accountId(accountId)
                .transactionType(transactionType)
                .amount(amount)
                .fee(fee)
                .balanceAfter(balanceAfter)
                .relatedAccountNumber(relatedAccountNumber)
                .createdAt(createdAt)
                .build();
    }

    /**
     * 입금 거래 생성
     */
    public static Transaction createDeposit(Long accountId, BigDecimal amount, BigDecimal balanceAfter, LocalDateTime createdAt) {
        return create(accountId, TransactionType.DEPOSIT, amount, BigDecimal.ZERO, balanceAfter, null, createdAt);
    }

    /**
     * 출금 거래 생성
     */
    public static Transaction createWithdrawal(Long accountId, BigDecimal amount, BigDecimal balanceAfter, LocalDateTime createdAt) {
        return create(accountId, TransactionType.WITHDRAWAL, amount, BigDecimal.ZERO, balanceAfter, null, createdAt);
    }

    /**
     * 이체 출금 거래 생성
     */
    public static Transaction createTransferOut(
            Long accountId,
            BigDecimal amount,
            BigDecimal fee,
            BigDecimal balanceAfter,
            String toAccountNumber,
            LocalDateTime createdAt
    ) {
        return create(accountId, TransactionType.TRANSFER_OUT, amount, fee, balanceAfter, toAccountNumber, createdAt);
    }

    /**
     * 이체 입금 거래 생성
     */
    public static Transaction createTransferIn(
            Long accountId,
            BigDecimal amount,
            BigDecimal balanceAfter,
            String fromAccountNumber,
            LocalDateTime createdAt
    ) {
        return create(accountId, TransactionType.TRANSFER_IN, amount, BigDecimal.ZERO, balanceAfter, fromAccountNumber, createdAt);
    }

    /**
     * ID 설정 (영속화 후 사용)
     */
    public Transaction withId(Long id) {
        return Transaction.builder()
                .id(id)
                .accountId(this.accountId)
                .transactionType(this.transactionType)
                .amount(this.amount)
                .fee(this.fee)
                .balanceAfter(this.balanceAfter)
                .relatedAccountNumber(this.relatedAccountNumber)
                .createdAt(this.createdAt)
                .build();
    }

    /**
     * 생성 시간 설정
     */
    public Transaction withCreatedAt(LocalDateTime createdAt) {
        return Transaction.builder()
                .id(this.id)
                .accountId(this.accountId)
                .transactionType(this.transactionType)
                .amount(this.amount)
                .fee(this.fee)
                .balanceAfter(this.balanceAfter)
                .relatedAccountNumber(this.relatedAccountNumber)
                .createdAt(createdAt)
                .build();
    }
}