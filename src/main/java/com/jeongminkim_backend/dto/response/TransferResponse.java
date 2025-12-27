package com.jeongminkim_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "이체 응답")
public class TransferResponse {

    @Schema(description = "이체 ID", example = "TRF-20240115-000001")
    private String transferId;

    @Schema(description = "출금 거래 정보")
    private TransactionResponse fromTransaction;

    @Schema(description = "입금 거래 정보")
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
