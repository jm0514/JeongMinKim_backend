package com.jeongminkim.application.service.policy;

import com.jeongminkim.application.common.time.TimeProvider;
import com.jeongminkim.core.domain.enums.TransactionType;
import com.jeongminkim.core.exception.BusinessException;
import com.jeongminkim.core.exception.ErrorCode;
import com.jeongminkim.core.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 출금 한도 체커
 * 일일 출금 한도: 1,000,000원
 */
@Component
@RequiredArgsConstructor
public class WithdrawalLimitChecker implements LimitChecker {

    private final TransactionRepository transactionRepository;
    private final TimeProvider timeProvider;

    private static final BigDecimal DAILY_WITHDRAWAL_LIMIT = new BigDecimal("1000000");

    @Override
    public void checkLimit(Long accountId, BigDecimal amount) {
        LocalDateTime startOfDay = timeProvider.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = timeProvider.now().toLocalDate().atTime(LocalTime.MAX);

        BigDecimal todayWithdrawalAmount = transactionRepository.sumAmountByAccountIdAndTypeAndDateRange(
                accountId,
                TransactionType.WITHDRAWAL,
                startOfDay,
                endOfDay
        );

        BigDecimal totalAmount = todayWithdrawalAmount.add(amount);

        if (totalAmount.compareTo(DAILY_WITHDRAWAL_LIMIT) > 0) {
            throw new BusinessException(ErrorCode.DAILY_WITHDRAWAL_LIMIT_EXCEEDED,
                    String.format("한도: %s원, 현재 사용: %s원, 요청: %s원",
                            DAILY_WITHDRAWAL_LIMIT, todayWithdrawalAmount, amount));
        }
    }
}