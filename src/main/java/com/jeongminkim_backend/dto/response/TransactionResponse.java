package com.jeongminkim_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeongminkim_backend.domain.entity.Transaction;
import com.jeongminkim_backend.domain.enums.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class TransactionResponse {

    private Long transactionId;
    
    private String accountNumber;
    
    private TransactionType transactionType;
    
    private BigDecimal amount;
    
    private BigDecimal fee;
    
    private BigDecimal balanceAfter;
    
    private String relatedAccountNumber;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public static TransactionResponse from(Transaction transaction, String accountNumber) {
        return TransactionResponse.builder()
                .transactionId(transaction.getId())
                .accountNumber(accountNumber)
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .fee(transaction.getFee())
                .balanceAfter(transaction.getBalanceAfter())
                .relatedAccountNumber(transaction.getRelatedAccountNumber())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
