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
@Schema(description = "입금 요청")
public class DepositRequest {

    @Schema(description = "입금할 계좌번호", example = "1234567890", requiredMode = REQUIRED)
    @NotBlank(message = "계좌번호는 필수입니다")
    private String accountNumber;

    @Schema(description = "입금 금액", example = "10000", requiredMode = REQUIRED, minimum = "0.01")
    @NotNull(message = "입금 금액은 필수입니다")
    @DecimalMin(value = "0.01", message = "입금 금액은 0보다 커야 합니다")
    private BigDecimal amount;
}
