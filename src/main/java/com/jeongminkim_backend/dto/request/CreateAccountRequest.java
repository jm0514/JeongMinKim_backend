package com.jeongminkim_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Getter
@NoArgsConstructor
@Schema(description = "계좌 생성 요청")
public class CreateAccountRequest {

    @Schema(description = "계좌번호", example = "1234567890", requiredMode = REQUIRED, minLength = 10, maxLength = 20)
    @NotBlank(message = "계좌번호는 필수입니다")
    @Size(min = 10, max = 20, message = "계좌번호는 10~20자리여야 합니다")
    private String accountNumber;

    @Schema(description = "예금주명", example = "홍길동", requiredMode = REQUIRED, minLength = 2, maxLength = 100)
    @NotBlank(message = "예금주명은 필수입니다")
    @Size(min = 2, max = 100, message = "예금주명은 2~100자여야 합니다")
    private String ownerName;
}
