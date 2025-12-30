package com.jeongminkim.domain.port.in;

import com.jeongminkim.domain.model.Transaction;

import java.util.List;

/**
 * 거래 내역 조회 Use Case (Inbound Port)
 */
public interface GetTransactionHistoryUseCase {

    /**
     * 계좌의 거래 내역 조회 (페이징)
     */
    TransactionPage getTransactionHistory(String accountNumber, int page, int size);

    /**
     * 페이징된 거래 내역
     */
    record TransactionPage(
            List<Transaction> transactions,
            long totalElements,
            int totalPages,
            int currentPage,
            int pageSize
    ) {}
}