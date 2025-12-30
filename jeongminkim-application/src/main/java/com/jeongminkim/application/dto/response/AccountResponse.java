package com.jeongminkim.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeongminkim.domain.model.Account;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "계좌 정보 응답")
public class AccountResponse {

    @Schema(description = "계좌 ID", example = "1")
    private Long id;

    @Schema(description = "계좌번호", example = "1234567890")
    private String accountNumber;

    @Schema(description = "예금주명", example = "홍길동")
    private String ownerName;

    @Schema(description = "잔액", example = "50000.00")
    private BigDecimal balance;

    @Schema(description = "계좌 생성일시", example = "2025-12-27T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "계좌 수정일시", example = "2025-12-27T11:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static AccountResponse from(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .ownerName(account.getOwnerName())
                .balance(account.getBalance())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
