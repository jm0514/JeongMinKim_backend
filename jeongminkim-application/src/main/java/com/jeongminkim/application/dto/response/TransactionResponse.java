package com.jeongminkim.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeongminkim.core.domain.entity.Transaction;
import com.jeongminkim.core.domain.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "거래 내역 응답")
public class TransactionResponse {

    @Schema(description = "거래 ID", example = "1")
    private Long transactionId;

    @Schema(description = "계좌번호", example = "1234567890")
    private String accountNumber;

    @Schema(description = "거래 유형", example = "DEPOSIT", allowableValues = {"DEPOSIT", "WITHDRAWAL", "TRANSFER_OUT", "TRANSFER_IN"})
    private TransactionType transactionType;

    @Schema(description = "거래 금액", example = "10000.00")
    private BigDecimal amount;

    @Schema(description = "수수료", example = "100.00")
    private BigDecimal fee;

    @Schema(description = "거래 후 잔액", example = "60000.00")
    private BigDecimal balanceAfter;

    @Schema(description = "상대방 계좌번호 (이체의 경우)", example = "0987654321", nullable = true)
    private String relatedAccountNumber;

    @Schema(description = "거래 일시", example = "2025-12-27T10:35:00")
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
