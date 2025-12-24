package com.jeongminkim_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateAccountRequest {

    @NotBlank(message = "계좌번호는 필수입니다")
    @Size(min = 10, max = 20, message = "계좌번호는 10~20자리여야 합니다")
    private String accountNumber;

    @NotBlank(message = "예금주명은 필수입니다")
    @Size(min = 2, max = 100, message = "예금주명은 2~100자여야 합니다")
    private String ownerName;
}
