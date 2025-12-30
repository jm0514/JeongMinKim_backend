package com.jeongminkim.application.service.policy;

import com.jeongminkim.domain.exception.DomainException;
import com.jeongminkim.domain.exception.ErrorType;
import com.jeongminkim.domain.model.TransactionType;
import com.jeongminkim.domain.port.out.TimePort;
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
@DisplayName("TransferLimitChecker 단위 테스트")
class TransferLimitCheckerTest {

    @Mock
    private TransactionPort transactionPort;

    @Mock
    private TimePort timePort;

    @InjectMocks
    private TransferLimitChecker transferLimitChecker;

    @Test
    @DisplayName("한도 체크 성공 - 한도 내 이체")
    void checkLimit_success_within_limit() {
        // given
        Long accountId = 1L;
        BigDecimal previousAmount = new BigDecimal("1500000");
        BigDecimal requestAmount = new BigDecimal("1000000");
        LocalDate today = LocalDate.of(2024, 1, 15);

        when(timePort.today()).thenReturn(today);
        when(transactionPort.sumAmountByAccountIdAndTypeAndDate(
                eq(accountId),
                eq(TransactionType.TRANSFER_OUT),
                any(LocalDate.class)
        )).thenReturn(previousAmount);

        // when & then
        // 1,500,000 + 1,000,000 = 2,500,000 < 3,000,000 (한도)
        assertThatCode(() -> transferLimitChecker.checkLimit(accountId, requestAmount))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("한도 체크 성공 - 한도 정확히 맞춤")
    void checkLimit_success_exactly_limit() {
        // given
        Long accountId = 1L;
        BigDecimal previousAmount = new BigDecimal("2000000");
        BigDecimal requestAmount = new BigDecimal("1000000");
        LocalDate today = LocalDate.of(2024, 1, 15);

        when(timePort.today()).thenReturn(today);
        when(transactionPort.sumAmountByAccountIdAndTypeAndDate(
                eq(accountId),
                eq(TransactionType.TRANSFER_OUT),
                any(LocalDate.class)
        )).thenReturn(previousAmount);

        // when & then
        // 2,000,000 + 1,000,000 = 3,000,000 (한도 정확히 맞춤)
        assertThatCode(() -> transferLimitChecker.checkLimit(accountId, requestAmount))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("한도 체크 실패 - 일일 이체 한도 초과")
    void checkLimit_fail_exceed_limit() {
        // given
        Long accountId = 1L;
        BigDecimal previousAmount = new BigDecimal("2000000");
        BigDecimal requestAmount = new BigDecimal("1500000");
        LocalDate today = LocalDate.of(2024, 1, 15);

        when(timePort.today()).thenReturn(today);
        when(transactionPort.sumAmountByAccountIdAndTypeAndDate(
                eq(accountId),
                eq(TransactionType.TRANSFER_OUT),
                any(LocalDate.class)
        )).thenReturn(previousAmount);

        // when & then
        // 2,000,000 + 1,500,000 = 3,500,000 > 3,000,000 (한도 초과)
        assertThatThrownBy(() -> transferLimitChecker.checkLimit(accountId, requestAmount))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.DAILY_TRANSFER_LIMIT_EXCEEDED)
                .hasMessageContaining("한도: 3000000원")
                .hasMessageContaining("현재 사용: 2000000원")
                .hasMessageContaining("요청: 1500000원");
    }
}