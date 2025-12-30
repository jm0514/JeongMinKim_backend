package com.jeongminkim.application.service.policy;

import com.jeongminkim.domain.exception.DomainException;
import com.jeongminkim.domain.exception.ErrorType;
import com.jeongminkim.domain.model.TransactionType;
import com.jeongminkim.domain.port.out.TransactionPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("WithdrawalLimitChecker 단위 테스트")
class WithdrawalLimitCheckerTest {

    @Mock
    private TransactionPort transactionPort;

    @InjectMocks
    private WithdrawalLimitChecker withdrawalLimitChecker;

    @Test
    @DisplayName("한도 체크 성공 - 한도 내 출금")
    void checkLimit_success_within_limit() {
        // given
        Long accountId = 1L;
        BigDecimal previousAmount = new BigDecimal("500000");
        BigDecimal requestAmount = new BigDecimal("400000");
        LocalDate today = LocalDate.of(2024, 1, 15);

        when(transactionPort.sumAmountByAccountIdAndTypeAndDate(
                eq(accountId),
                eq(TransactionType.WITHDRAWAL),
                any(LocalDate.class)
        )).thenReturn(previousAmount);

        // when & then
        // 500,000 + 400,000 = 900,000 < 1,000,000 (한도)
        assertThatCode(() -> withdrawalLimitChecker.checkLimit(accountId, requestAmount, today))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("한도 체크 성공 - 한도 정확히 맞춤")
    void checkLimit_success_exactly_limit() {
        // given
        Long accountId = 1L;
        BigDecimal previousAmount = new BigDecimal("700000");
        BigDecimal requestAmount = new BigDecimal("300000");
        LocalDate today = LocalDate.of(2024, 1, 15);

        when(transactionPort.sumAmountByAccountIdAndTypeAndDate(
                eq(accountId),
                eq(TransactionType.WITHDRAWAL),
                any(LocalDate.class)
        )).thenReturn(previousAmount);

        // when & then
        // 700,000 + 300,000 = 1,000,000 (한도 정확히 맞춤)
        assertThatCode(() -> withdrawalLimitChecker.checkLimit(accountId, requestAmount, today))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("한도 체크 실패 - 일일 출금 한도 초과")
    void checkLimit_fail_exceed_limit() {
        // given
        Long accountId = 1L;
        BigDecimal previousAmount = new BigDecimal("700000");
        BigDecimal requestAmount = new BigDecimal("400000");
        LocalDate today = LocalDate.of(2024, 1, 15);

        when(transactionPort.sumAmountByAccountIdAndTypeAndDate(
                eq(accountId),
                eq(TransactionType.WITHDRAWAL),
                any(LocalDate.class)
        )).thenReturn(previousAmount);

        // when & then
        // 700,000 + 400,000 = 1,100,000 > 1,000,000 (한도 초과)
        assertThatThrownBy(() -> withdrawalLimitChecker.checkLimit(accountId, requestAmount, today))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.DAILY_WITHDRAWAL_LIMIT_EXCEEDED)
                .hasMessageContaining("한도: 1000000원")
                .hasMessageContaining("현재 사용: 700000원")
                .hasMessageContaining("요청: 400000원");
    }
}
