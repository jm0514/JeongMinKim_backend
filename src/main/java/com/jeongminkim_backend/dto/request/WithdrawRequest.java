package com.jeongminkim_backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class WithdrawRequest {

    @NotBlank(message = "계좌번호는 필수입니다")
    private String accountNumber;

    @NotNull(message = "출금 금액은 필수입니다")
    @DecimalMin(value = "0.01", message = "출금 금액은 0보다 커야 합니다")
    private BigDecimal amount;
}
