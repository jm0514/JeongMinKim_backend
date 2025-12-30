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
 * 이체 한도 체커
 * 일일 이체 한도: 3,000,000원
 */
@Component
@RequiredArgsConstructor
public class TransferLimitChecker implements LimitChecker {

    private final TransactionPort transactionPort;

    private static final BigDecimal DAILY_TRANSFER_LIMIT = new BigDecimal("3000000");

    @Override
    public void checkLimit(Long accountId, BigDecimal amount, LocalDate transactionDate) {
        BigDecimal todayTransferAmount = transactionPort.sumAmountByAccountIdAndTypeAndDate(
                accountId,
                TransactionType.TRANSFER_OUT,
                transactionDate
        );

        BigDecimal totalAmount = todayTransferAmount.add(amount);

        if (totalAmount.compareTo(DAILY_TRANSFER_LIMIT) > 0) {
            throw new DomainException(ErrorType.DAILY_TRANSFER_LIMIT_EXCEEDED,
                    String.format("한도: %s원, 현재 사용: %s원, 요청: %s원",
                            DAILY_TRANSFER_LIMIT, todayTransferAmount, amount));
        }
    }
}
