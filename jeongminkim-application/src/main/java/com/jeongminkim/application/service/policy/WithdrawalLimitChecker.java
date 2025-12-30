package com.jeongminkim.application.service.policy;

import com.jeongminkim.domain.exception.DomainException;
import com.jeongminkim.domain.exception.ErrorType;
import com.jeongminkim.domain.model.TransactionType;
import com.jeongminkim.domain.port.out.TransactionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 출금 한도 체커
 * 일일 출금 한도: 1,000,000원
 */
@Component
@RequiredArgsConstructor
public class WithdrawalLimitChecker implements LimitChecker {

    private final TransactionPort transactionPort;

    private static final BigDecimal DAILY_WITHDRAWAL_LIMIT = new BigDecimal("1000000");

    @Override
    public void checkLimit(Long accountId, BigDecimal amount, LocalDate transactionDate) {
        BigDecimal todayWithdrawalAmount = transactionPort.sumAmountByAccountIdAndTypeAndDate(
                accountId,
                TransactionType.WITHDRAWAL,
                transactionDate
        );

        BigDecimal totalAmount = todayWithdrawalAmount.add(amount);

        if (totalAmount.compareTo(DAILY_WITHDRAWAL_LIMIT) > 0) {
            throw new DomainException(ErrorType.DAILY_WITHDRAWAL_LIMIT_EXCEEDED,
                    String.format("한도: %s원, 현재 사용: %s원, 요청: %s원",
                            DAILY_WITHDRAWAL_LIMIT, todayWithdrawalAmount, amount));
        }
    }
}
