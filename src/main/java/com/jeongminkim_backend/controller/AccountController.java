package com.jeongminkim_backend.controller;

import com.jeongminkim_backend.controller.api.AccountApi;
import com.jeongminkim_backend.dto.CommonResponse;
import com.jeongminkim_backend.dto.CommonResponseFactory;
import com.jeongminkim_backend.dto.request.CreateAccountRequest;
import com.jeongminkim_backend.dto.response.AccountResponse;
import com.jeongminkim_backend.dto.response.ResponseMessage;
import com.jeongminkim_backend.service.AccountService;
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

    private final AccountService accountService;
    private final CommonResponseFactory commonResponseFactory;

    /**
     * 계좌 생성
     * POST /api/v1/accounts
     */
    @PostMapping
    public ResponseEntity<CommonResponse<AccountResponse>> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        log.info("POST /api/v1/accounts - 계좌 생성 요청: {}", request.getAccountNumber());
        AccountResponse response = accountService.createAccount(request);
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
        AccountResponse response = accountService.getAccount(accountNumber);
        return ResponseEntity.ok(commonResponseFactory.success(response, ResponseMessage.ACCOUNT_RETRIEVED));
    }

    /**
     * 계좌 삭제
     * DELETE /api/v1/accounts/{accountNumber}
     */
    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountNumber) {
        log.info("DELETE /api/v1/accounts/{} - 계좌 삭제 요청", accountNumber);
        accountService.deleteAccount(accountNumber);
        return ResponseEntity.noContent().build();
    }
}
