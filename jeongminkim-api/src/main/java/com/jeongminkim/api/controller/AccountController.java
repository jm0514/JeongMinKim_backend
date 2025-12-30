package com.jeongminkim.api.controller;

import com.jeongminkim.api.controller.api.AccountApi;
import com.jeongminkim.application.dto.CommonResponse;
import com.jeongminkim.application.dto.CommonResponseFactory;
import com.jeongminkim.application.dto.request.CreateAccountRequest;
import com.jeongminkim.application.dto.response.AccountResponse;
import com.jeongminkim.application.dto.response.ResponseMessage;
import com.jeongminkim.domain.model.Account;
import com.jeongminkim.domain.port.in.CreateAccountUseCase;
import com.jeongminkim.domain.port.in.DeleteAccountUseCase;
import com.jeongminkim.domain.port.in.GetAccountUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController implements AccountApi {

    private final CreateAccountUseCase createAccountUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final DeleteAccountUseCase deleteAccountUseCase;
    private final CommonResponseFactory commonResponseFactory;

    /**
     * 계좌 생성
     * POST /api/v1/accounts
     */
    @PostMapping
    public ResponseEntity<CommonResponse<AccountResponse>> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        log.info("POST /api/v1/accounts - 계좌 생성 요청: {}", request.getAccountNumber());
        Account account = createAccountUseCase.createAccount(request.getAccountNumber(), request.getOwnerName());
        AccountResponse response = AccountResponse.from(account);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commonResponseFactory.success(response, ResponseMessage.ACCOUNT_CREATED));
    }

    /**
     * 계좌 조회
     * GET /api/v1/accounts/{accountNumber}
     */
    @GetMapping("/{accountNumber}")
    public ResponseEntity<CommonResponse<AccountResponse>> getAccount(@PathVariable String accountNumber) {
        log.info("GET /api/v1/accounts/{} - 계좌 조회 요청", accountNumber);
        Account account = getAccountUseCase.getAccount(accountNumber);
        AccountResponse response = AccountResponse.from(account);
        return ResponseEntity.ok(commonResponseFactory.success(response, ResponseMessage.ACCOUNT_RETRIEVED));
    }

    /**
     * 계좌 삭제
     * DELETE /api/v1/accounts/{accountNumber}
     */
    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountNumber) {
        log.info("DELETE /api/v1/accounts/{} - 계좌 삭제 요청", accountNumber);
        deleteAccountUseCase.deleteAccount(accountNumber);
        return ResponseEntity.noContent().build();
    }
}