package com.jeongminkim_backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransferResponse {

    private String transferId;
    
    private TransactionResponse fromTransaction;
    
    private TransactionResponse toTransaction;

    public static TransferResponse of(
            String transferId,
            TransactionResponse fromTransaction,
            TransactionResponse toTransaction
    ) {
        return TransferResponse.builder()
                .transferId(transferId)
                .fromTransaction(fromTransaction)
                .toTransaction(toTransaction)
                .build();
    }
}
