package com.jeongminkim.domain.port.out;

import com.jeongminkim.domain.model.Transaction;
import com.jeongminkim.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 거래 영속성 포트 (Outbound Port)
 * Infrastructure 계층에서 구현
 */
public interface TransactionPort {

    /**
     * 거래 내역 저장
     */
    Transaction save(Transaction transaction);

    /**
     * 계좌의 모든 거래 내역 조회 (페이징)
     */
    List<Transaction> findAllByAccountId(Long accountId, int page, int size);

    /**
     * 계좌의 특정 타입 거래 합계 조회 (특정 날짜)
     */
    BigDecimal sumAmountByAccountIdAndTypeAndDate(
            Long accountId,
            TransactionType transactionType,
            LocalDate date
    );

    /**
     * 계좌의 거래 총 개수
     */
    long countByAccountId(Long accountId);
}