package com.jeongminkim.application.service.policy;

import java.math.BigDecimal;

/**
 * 수수료 계산 전략 인터페이스
 * 새로운 수수료 정책 추가 시 이 인터페이스를 구현하면 됨
 */
public interface FeeCalculator {

    /**
     * 수수료 계산
     * @param amount 거래 금액
     * @return 수수료
     */
    BigDecimal calculate(BigDecimal amount);
}
