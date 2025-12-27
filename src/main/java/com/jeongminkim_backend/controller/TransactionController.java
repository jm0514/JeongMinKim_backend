package com.jeongminkim_backend.controller;

import com.jeongminkim_backend.dto.ApiResponse;
import com.jeongminkim_backend.dto.request.DepositRequest;
import com.jeongminkim_backend.dto.request.TransferRequest;
import com.jeongminkim_backend.dto.request.WithdrawRequest;
import com.jeongminkim_backend.dto.response.ResponseMessage;
import com.jeongminkim_backend.dto.response.TransactionResponse;
import com.jeongminkim_backend.dto.response.TransferResponse;
import com.jeongminkim_backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * 입금
     * POST /api/v1/transactions/deposit
     */
    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(@Valid @RequestBody DepositRequest request) {
        log.info("POST /api/v1/transactions/deposit - 입금 요청: 계좌={}, 금액={}",
                request.getAccountNumber(), request.getAmount());
        TransactionResponse response = transactionService.deposit(request);
        return ResponseEntity.ok(ApiResponse.success(response, ResponseMessage.DEPOSIT_SUCCESS));
    }

    /**
     * 출금
     * POST /api/v1/transactions/withdraw
     */
    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(@Valid @RequestBody WithdrawRequest request) {
        log.info("POST /api/v1/transactions/withdraw - 출금 요청: 계좌={}, 금액={}",
                request.getAccountNumber(), request.getAmount());
        TransactionResponse response = transactionService.withdraw(request);
        return ResponseEntity.ok(ApiResponse.success(response, ResponseMessage.WITHDRAWAL_SUCCESS));
    }

    /**
     * 이체
     * POST /api/v1/transactions/transfer
     */
    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransferResponse>> transfer(@Valid @RequestBody TransferRequest request) {
        log.info("POST /api/v1/transactions/transfer - 이체 요청: 출금계좌={}, 입금계좌={}, 금액={}",
                request.getFromAccountNumber(), request.getToAccountNumber(), request.getAmount());
        TransferResponse response = transactionService.transfer(request);
        return ResponseEntity.ok(ApiResponse.success(response, ResponseMessage.TRANSFER_SUCCESS));
    }

    /**
     * 거래 내역 조회 (페이징)
     * GET /api/v1/transactions?accountNumber={accountNumber}&page={page}&size={size}
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactions(
            @RequestParam String accountNumber,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("GET /api/v1/transactions - 거래 내역 조회: 계좌={}, page={}, size={}",
                accountNumber, pageable.getPageNumber(), pageable.getPageSize());
        Page<TransactionResponse> response = transactionService.getTransactions(accountNumber, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, ResponseMessage.TRANSACTION_HISTORY_RETRIEVED));
    }
}
