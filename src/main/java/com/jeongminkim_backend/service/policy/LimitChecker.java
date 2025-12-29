package com.jeongminkim_backend.service.policy;

import java.math.BigDecimal;

/**
 * 한도 체크 전략 인터페이스
 * 새로운 한도 정책 추가 시 이 인터페이스를 구현하면 됨
 */
public interface LimitChecker {

    /**
     * 한도 초과 여부 확인
     * @param accountId 계좌 ID
     * @param amount 요청 금액
     * @throws com.jeongminkim_backend.exception.BusinessException 한도 초과 시
     */
    void checkLimit(Long accountId, BigDecimal amount);
}