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
 * 이체 한도 체커
 * 일일 이체 한도: 3,000,000원
 */
@Component
@RequiredArgsConstructor
public class TransferLimitChecker implements LimitChecker {

    private final TransactionRepository transactionRepository;
    private final TimeProvider timeProvider;

    private static final BigDecimal DAILY_TRANSFER_LIMIT = new BigDecimal("3000000");

    @Override
    public void checkLimit(Long accountId, BigDecimal amount) {
        LocalDateTime startOfDay = timeProvider.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = timeProvider.now().toLocalDate().atTime(LocalTime.MAX);

        BigDecimal todayTransferAmount = transactionRepository.sumAmountByAccountIdAndTypeAndDateRange(
                accountId,
                TransactionType.TRANSFER_OUT,
                startOfDay,
                endOfDay
        );

        BigDecimal totalAmount = todayTransferAmount.add(amount);

        if (totalAmount.compareTo(DAILY_TRANSFER_LIMIT) > 0) {
            throw new BusinessException(ErrorCode.DAILY_TRANSFER_LIMIT_EXCEEDED,
                    String.format("한도: %s원, 현재 사용: %s원, 요청: %s원",
                            DAILY_TRANSFER_LIMIT, todayTransferAmount, amount));
        }
    }
}