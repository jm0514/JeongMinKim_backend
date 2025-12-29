package com.jeongminkim.application.service.policy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PercentageFeeCalculator 단위 테스트")
class PercentageFeeCalculatorTest {

    private final PercentageFeeCalculator feeCalculator = new PercentageFeeCalculator();

    @Test
    @DisplayName("수수료 계산 성공 - 10,000원의 1%는 100원")
    void calculate_success() {
        // given
        BigDecimal amount = new BigDecimal("10000");

        // when
        BigDecimal fee = feeCalculator.calculate(amount);

        // then
        assertThat(fee).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("수수료 계산 - 반올림 검증 (999.5원의 1%는 9.995 -> 10.00)")
    void calculate_rounding_up() {
        // given
        BigDecimal amount = new BigDecimal("999.5");

        // when
        BigDecimal fee = feeCalculator.calculate(amount);

        // then
        assertThat(fee).isEqualByComparingTo("10.00");
        assertThat(fee.scale()).isEqualTo(2); // 소수점 2자리
    }

    @Test
    @DisplayName("수수료 계산 - 반올림 검증 (994.4원의 1%는 9.944 -> 9.94)")
    void calculate_rounding_down() {
        // given
        BigDecimal amount = new BigDecimal("994.4");

        // when
        BigDecimal fee = feeCalculator.calculate(amount);

        // then
        assertThat(fee).isEqualByComparingTo("9.94");
        assertThat(fee.scale()).isEqualTo(2); // 소수점 2자리
    }

    @Test
    @DisplayName("수수료 계산 - 1원의 1%는 0.01원")
    void calculate_minimum_amount() {
        // given
        BigDecimal amount = new BigDecimal("1");

        // when
        BigDecimal fee = feeCalculator.calculate(amount);

        // then
        assertThat(fee).isEqualByComparingTo("0.01");
    }

    @Test
    @DisplayName("수수료 계산 실패 - null")
    void calculate_fail_null() {
        // when & then
        assertThatThrownBy(() -> feeCalculator.calculate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("금액은 0보다 커야 합니다");
    }

    @Test
    @DisplayName("수수료 계산 실패 - 0원 이하")
    void calculate_fail_invalid_amount() {
        // when & then - 0원
        assertThatThrownBy(() -> feeCalculator.calculate(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("금액은 0보다 커야 합니다");

        // when & then - 음수
        assertThatThrownBy(() -> feeCalculator.calculate(new BigDecimal("-1000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("금액은 0보다 커야 합니다");
    }
}
