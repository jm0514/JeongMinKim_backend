package com.jeongminkim_backend.controller;

import com.jeongminkim_backend.dto.ApiResponse;
import com.jeongminkim_backend.dto.request.CreateAccountRequest;
import com.jeongminkim_backend.dto.response.AccountResponse;
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
public class AccountController {

    private final AccountService accountService;

    /**
     * 계좌 생성
     * POST /api/v1/accounts
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        log.info("POST /api/v1/accounts - 계좌 생성 요청: {}", request.getAccountNumber());
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "계좌가 성공적으로 생성되었습니다"));
    }

    /**
     * 계좌 조회
     * GET /api/v1/accounts/{accountNumber}
     */
    @GetMapping("/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(@PathVariable String accountNumber) {
        log.info("GET /api/v1/accounts/{} - 계좌 조회 요청", accountNumber);
        AccountResponse response = accountService.getAccount(accountNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 계좌 삭제
     * DELETE /api/v1/accounts/{accountNumber}
     */
    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@PathVariable String accountNumber) {
        log.info("DELETE /api/v1/accounts/{} - 계좌 삭제 요청", accountNumber);
        accountService.deleteAccount(accountNumber);
        return ResponseEntity.ok(ApiResponse.success("계좌가 성공적으로 삭제되었습니다"));
    }
}
