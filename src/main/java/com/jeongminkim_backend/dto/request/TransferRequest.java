package com.jeongminkim_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Getter
@NoArgsConstructor
@Schema(description = "이체 요청")
public class TransferRequest {

    @Schema(description = "출금 계좌번호", example = "1234567890", requiredMode = REQUIRED)
    @NotBlank(message = "출금 계좌번호는 필수입니다")
    private String fromAccountNumber;

    @Schema(description = "입금 계좌번호", example = "0987654321", requiredMode = REQUIRED)
    @NotBlank(message = "입금 계좌번호는 필수입니다")
    private String toAccountNumber;

    @Schema(description = "이체 금액 (수수료 1% 별도, 일일 한도: 3,000,000원)", example = "100000", requiredMode = REQUIRED, minimum = "0.01")
    @NotNull(message = "이체 금액은 필수입니다")
    @DecimalMin(value = "0.01", message = "이체 금액은 0보다 커야 합니다")
    private BigDecimal amount;
}
