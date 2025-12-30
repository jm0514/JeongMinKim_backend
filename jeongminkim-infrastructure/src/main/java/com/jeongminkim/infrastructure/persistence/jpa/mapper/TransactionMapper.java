package com.jeongminkim.infrastructure.persistence.jpa.mapper;

import com.jeongminkim.domain.model.Transaction;
import com.jeongminkim.infrastructure.persistence.jpa.entity.TransactionEntity;
import org.springframework.stereotype.Component;

/**
 * Transaction Domain ↔ TransactionEntity 매퍼
 */
@Component
public class TransactionMapper {

    /**
     * Domain → Entity
     */
    public TransactionEntity toEntity(Transaction transaction) {
        return TransactionEntity.builder()
                .id(transaction.getId())
                .accountId(transaction.getAccountId())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .fee(transaction.getFee())
                .balanceAfter(transaction.getBalanceAfter())
                .relatedAccountNumber(transaction.getRelatedAccountNumber())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    /**
     * Entity → Domain
     */
    public Transaction toDomain(TransactionEntity entity) {
        return Transaction.builder()
                .id(entity.getId())
                .accountId(entity.getAccountId())
                .transactionType(entity.getTransactionType())
                .amount(entity.getAmount())
                .fee(entity.getFee())
                .balanceAfter(entity.getBalanceAfter())
                .relatedAccountNumber(entity.getRelatedAccountNumber())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}