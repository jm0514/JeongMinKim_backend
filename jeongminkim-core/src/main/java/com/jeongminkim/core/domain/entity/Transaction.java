package com.jeongminkim.core.domain.entity;

import com.jeongminkim.core.domain.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal fee;

    @Column(name = "balance_after", nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "related_account_number", length = 20)
    private String relatedAccountNumber;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
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
            String relatedAccountNumber
    ) {
        Transaction transaction = new Transaction();
        transaction.accountId = accountId;
        transaction.transactionType = transactionType;
        transaction.amount = amount;
        transaction.fee = fee;
        transaction.balanceAfter = balanceAfter;
        transaction.relatedAccountNumber = relatedAccountNumber;
        return transaction;
    }

    /**
     * 입금 거래 생성
     */
    public static Transaction createDeposit(Long accountId, BigDecimal amount, BigDecimal balanceAfter) {
        return create(accountId, TransactionType.DEPOSIT, amount, BigDecimal.ZERO, balanceAfter, null);
    }

    /**
     * 출금 거래 생성
     */
    public static Transaction createWithdrawal(Long accountId, BigDecimal amount, BigDecimal balanceAfter) {
        return create(accountId, TransactionType.WITHDRAWAL, amount, BigDecimal.ZERO, balanceAfter, null);
    }

    /**
     * 이체 출금 거래 생성
     */
    public static Transaction createTransferOut(
            Long accountId,
            BigDecimal amount,
            BigDecimal fee,
            BigDecimal balanceAfter,
            String toAccountNumber
    ) {
        return create(accountId, TransactionType.TRANSFER_OUT, amount, fee, balanceAfter, toAccountNumber);
    }

    /**
     * 이체 입금 거래 생성
     */
    public static Transaction createTransferIn(
            Long accountId,
            BigDecimal amount,
            BigDecimal balanceAfter,
            String fromAccountNumber
    ) {
        return create(accountId, TransactionType.TRANSFER_IN, amount, BigDecimal.ZERO, balanceAfter, fromAccountNumber);
    }
}
