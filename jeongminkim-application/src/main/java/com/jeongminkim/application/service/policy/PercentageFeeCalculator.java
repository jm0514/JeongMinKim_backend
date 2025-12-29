package com.jeongminkim.application.service.policy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 퍼센트 기반 수수료 계산기
 * 이체 금액의 일정 비율을 수수료로 계산
 */
@Component
public class PercentageFeeCalculator implements FeeCalculator {

    private static final BigDecimal FEE_RATE = new BigDecimal("0.01"); // 1%

    @Override
    public BigDecimal calculate(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("금액은 0보다 커야 합니다");
        }

        return amount.multiply(FEE_RATE)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
