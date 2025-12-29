package com.jeongminkim_backend.service.policy;

import com.jeongminkim_backend.common.time.TimeProvider;
import com.jeongminkim_backend.domain.enums.TransactionType;
import com.jeongminkim_backend.exception.BusinessException;
import com.jeongminkim_backend.exception.ErrorCode;
import com.jeongminkim_backend.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("WithdrawalLimitChecker 단위 테스트")
class WithdrawalLimitCheckerTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private WithdrawalLimitChecker withdrawalLimitChecker;

    @Test
    @DisplayName("한도 체크 성공 - 한도 내 출금")
    void checkLimit_success_within_limit() {
        // given
        Long accountId = 1L;
        BigDecimal previousAmount = new BigDecimal("500000");
        BigDecimal requestAmount = new BigDecimal("400000");
        LocalDateTime now = LocalDateTime.of(2024, 1, 15, 14, 30);

        when(timeProvider.now()).thenReturn(now);
        when(transactionRepository.sumAmountByAccountIdAndTypeAndDateRange(
                eq(accountId),
                eq(TransactionType.WITHDRAWAL),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(previousAmount);

        // when & then
        // 500,000 + 400,000 = 900,000 < 1,000,000 (한도)
        assertThatCode(() -> withdrawalLimitChecker.checkLimit(accountId, requestAmount))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("한도 체크 성공 - 한도 정확히 맞춤")
    void checkLimit_success_exactly_limit() {
        // given
        Long accountId = 1L;
        BigDecimal previousAmount = new BigDecimal("700000");
        BigDecimal requestAmount = new BigDecimal("300000");
        LocalDateTime now = LocalDateTime.of(2024, 1, 15, 14, 30);

        when(timeProvider.now()).thenReturn(now);
        when(transactionRepository.sumAmountByAccountIdAndTypeAndDateRange(
                eq(accountId),
                eq(TransactionType.WITHDRAWAL),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(previousAmount);

        // when & then
        // 700,000 + 300,000 = 1,000,000 (한도 정확히 맞춤)
        assertThatCode(() -> withdrawalLimitChecker.checkLimit(accountId, requestAmount))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("한도 체크 실패 - 일일 출금 한도 초과")
    void checkLimit_fail_exceed_limit() {
        // given
        Long accountId = 1L;
        BigDecimal previousAmount = new BigDecimal("700000");
        BigDecimal requestAmount = new BigDecimal("400000");
        LocalDateTime now = LocalDateTime.of(2024, 1, 15, 14, 30);

        when(timeProvider.now()).thenReturn(now);
        when(transactionRepository.sumAmountByAccountIdAndTypeAndDateRange(
                eq(accountId),
                eq(TransactionType.WITHDRAWAL),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(previousAmount);

        // when & then
        // 700,000 + 400,000 = 1,100,000 > 1,000,000 (한도 초과)
        assertThatThrownBy(() -> withdrawalLimitChecker.checkLimit(accountId, requestAmount))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DAILY_WITHDRAWAL_LIMIT_EXCEEDED)
                .hasMessageContaining("한도: 1000000원")
                .hasMessageContaining("현재 사용: 700000원")
                .hasMessageContaining("요청: 400000원");
    }
}
