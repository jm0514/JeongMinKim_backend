package com.jeongminkim.api.controller;

import com.jeongminkim.api.controller.api.TransactionApi;
import com.jeongminkim.application.dto.CommonResponse;
import com.jeongminkim.application.dto.CommonResponseFactory;
import com.jeongminkim.application.dto.request.DepositRequest;
import com.jeongminkim.application.dto.request.TransferRequest;
import com.jeongminkim.application.dto.request.WithdrawRequest;
import com.jeongminkim.application.dto.response.ResponseMessage;
import com.jeongminkim.application.dto.response.TransactionResponse;
import com.jeongminkim.application.dto.response.TransferResponse;
import com.jeongminkim.application.port.in.DepositUseCase;
import com.jeongminkim.application.port.in.GetTransactionHistoryUseCase;
import com.jeongminkim.application.port.in.TransferUseCase;
import com.jeongminkim.application.port.in.WithdrawUseCase;
import com.jeongminkim.domain.model.Transaction;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController implements TransactionApi {

    private final DepositUseCase depositUseCase;
    private final WithdrawUseCase withdrawUseCase;
    private final TransferUseCase transferUseCase;
    private final GetTransactionHistoryUseCase getTransactionHistoryUseCase;
    private final CommonResponseFactory commonResponseFactory;

    /**
     * 입금
     * POST /api/v1/transactions/deposit
     */
    @PostMapping("/deposit")
    public ResponseEntity<CommonResponse<TransactionResponse>> deposit(@Valid @RequestBody DepositRequest request) {
        log.info("POST /api/v1/transactions/deposit - 입금 요청: 계좌={}, 금액={}",
                request.getAccountNumber(), request.getAmount());
        Transaction transaction = depositUseCase.deposit(request.getAccountNumber(), request.getAmount());
        TransactionResponse response = TransactionResponse.from(transaction, request.getAccountNumber());
        return ResponseEntity.ok(commonResponseFactory.success(response, ResponseMessage.DEPOSIT_SUCCESS));
    }

    /**
     * 출금
     * POST /api/v1/transactions/withdraw
     */
    @PostMapping("/withdraw")
    public ResponseEntity<CommonResponse<TransactionResponse>> withdraw(@Valid @RequestBody WithdrawRequest request) {
        log.info("POST /api/v1/transactions/withdraw - 출금 요청: 계좌={}, 금액={}",
                request.getAccountNumber(), request.getAmount());
        Transaction transaction = withdrawUseCase.withdraw(request.getAccountNumber(), request.getAmount());
        TransactionResponse response = TransactionResponse.from(transaction, request.getAccountNumber());
        return ResponseEntity.ok(commonResponseFactory.success(response, ResponseMessage.WITHDRAWAL_SUCCESS));
    }

    /**
     * 이체
     * POST /api/v1/transactions/transfer
     */
    @PostMapping("/transfer")
    public ResponseEntity<CommonResponse<TransferResponse>> transfer(@Valid @RequestBody TransferRequest request) {
        log.info("POST /api/v1/transactions/transfer - 이체 요청: 출금계좌={}, 입금계좌={}, 금액={}",
                request.getFromAccountNumber(), request.getToAccountNumber(), request.getAmount());

        TransferUseCase.TransferResult result = transferUseCase.transfer(
                request.getFromAccountNumber(),
                request.getToAccountNumber(),
                request.getAmount()
        );

        // Transfer ID 생성
        String transferId = "TRF-" + System.currentTimeMillis();

        // TransactionResponse 생성
        TransactionResponse fromTxResponse = TransactionResponse.from(
                result.fromTransaction(),
                request.getFromAccountNumber()
        );
        TransactionResponse toTxResponse = TransactionResponse.from(
                result.toTransaction(),
                request.getToAccountNumber()
        );

        // TransferResponse 생성
        TransferResponse response = TransferResponse.of(transferId, fromTxResponse, toTxResponse);

        return ResponseEntity.ok(commonResponseFactory.success(response, ResponseMessage.TRANSFER_SUCCESS));
    }

    /**
     * 거래 내역 조회 (페이징)
     * GET /api/v1/transactions?accountNumber={accountNumber}&page={page}&size={size}
     */
    @GetMapping
    public ResponseEntity<CommonResponse<Page<TransactionResponse>>> getTransactions(
            @RequestParam String accountNumber,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("GET /api/v1/transactions - 거래 내역 조회: 계좌={}, page={}, size={}",
                accountNumber, pageable.getPageNumber(), pageable.getPageSize());

        GetTransactionHistoryUseCase.TransactionPage transactionPage =
                getTransactionHistoryUseCase.getTransactionHistory(
                        accountNumber,
                        pageable.getPageNumber(),
                        pageable.getPageSize()
                );

        List<TransactionResponse> content = transactionPage.transactions().stream()
                .map(tx -> TransactionResponse.from(tx, accountNumber))
                .collect(Collectors.toList());

        Page<TransactionResponse> page = new PageImpl<>(
                content,
                pageable,
                transactionPage.totalElements()
        );

        return ResponseEntity.ok(commonResponseFactory.success(page, ResponseMessage.TRANSACTION_HISTORY_RETRIEVED));
    }
}