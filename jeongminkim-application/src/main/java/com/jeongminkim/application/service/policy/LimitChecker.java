package com.jeongminkim.application.service.policy;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 한도 체크 전략 인터페이스
 * 새로운 한도 정책 추가 시 이 인터페이스를 구현하면 됨
 */
public interface LimitChecker {

    /**
     * 한도 초과 여부 확인
     * @param accountId 계좌 ID
     * @param amount 요청 금액
     * @param transactionDate 거래 날짜 (자정 경계 시간 문제 방지를 위해 고정된 날짜 전달)
     * @throws com.jeongminkim.domain.exception.DomainException 한도 초과 시
     */
    void checkLimit(Long accountId, BigDecimal amount, LocalDate transactionDate);
}