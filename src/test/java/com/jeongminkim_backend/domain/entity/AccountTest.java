package com.jeongminkim_backend.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Account 엔티티 테스트")
class AccountTest {

    @Test
    @DisplayName("입금 성공")
    void deposit_success() {
        // given
        Account account = Account.create("1234567890", "홍길동");
        BigDecimal depositAmount = new BigDecimal("10000");

        // when
        account.deposit(depositAmount);

        // then
        assertThat(account.getBalance()).isEqualByComparingTo("10000");
    }

    @Test
    @DisplayName("입금 실패 - 0원")
    void deposit_fail_zero_amount() {
        // given
        Account account = Account.create("1234567890", "홍길동");
        BigDecimal zeroAmount = BigDecimal.ZERO;

        // when & then
        assertThatThrownBy(() -> account.deposit(zeroAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("금액은 0보다 커야 합니다");
    }

    @Test
    @DisplayName("입금 실패 - 음수")
    void deposit_fail_negative_amount() {
        // given
        Account account = Account.create("1234567890", "홍길동");
        BigDecimal negativeAmount = new BigDecimal("-1000");

        // when & then
        assertThatThrownBy(() -> account.deposit(negativeAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("금액은 0보다 커야 합니다");
    }

    @Test
    @DisplayName("입금 실패 - null")
    void deposit_fail_null_amount() {
        // given
        Account account = Account.create("1234567890", "홍길동");

        // when & then
        assertThatThrownBy(() -> account.deposit(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("금액은 0보다 커야 합니다");
    }

    @Test
    @DisplayName("출금 성공")
    void withdraw_success() {
        // given
        Account account = Account.create("1234567890", "홍길동");
        account.deposit(new BigDecimal("50000"));
        BigDecimal withdrawAmount = new BigDecimal("20000");

        // when
        account.withdraw(withdrawAmount);

        // then
        assertThat(account.getBalance()).isEqualByComparingTo("30000");
    }

    @Test
    @DisplayName("출금 실패 - 잔액 부족")
    void withdraw_fail_insufficient_balance() {
        // given
        Account account = Account.create("1234567890", "홍길동");
        account.deposit(new BigDecimal("10000"));
        BigDecimal withdrawAmount = new BigDecimal("20000");

        // when & then
        assertThatThrownBy(() -> account.withdraw(withdrawAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잔액이 부족합니다");
    }

    @Test
    @DisplayName("출금 실패 - 유효하지 않은 금액")
    void withdraw_fail_invalid_amount() {
        // given
        Account account = Account.create("1234567890", "홍길동");
        account.deposit(new BigDecimal("10000"));

        // when & then
        assertThatThrownBy(() -> account.withdraw(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("금액은 0보다 커야 합니다");
    }

    @Test
    @DisplayName("이체 수수료 계산 - 1%")
    void calculateTransferFee_correct_percentage() {
        // given
        Account account = Account.create("1234567890", "홍길동");
        BigDecimal transferAmount = new BigDecimal("10000");

        // when
        BigDecimal fee = account.calculateTransferFee(transferAmount);

        // then
        assertThat(fee).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("이체 수수료 계산 - 반올림")
    void calculateTransferFee_rounding() {
        // given
        Account account = Account.create("1234567890", "홍길동");
        BigDecimal transferAmount = new BigDecimal("10005");

        // when
        BigDecimal fee = account.calculateTransferFee(transferAmount);

        // then
        // 10005 * 0.01 = 100.05 → 반올림 → 100.05 (소수점 둘째자리까지)
        assertThat(fee).isEqualByComparingTo("100.05");
    }

    @Test
    @DisplayName("이체 수수료 계산 실패 - 유효하지 않은 금액")
    void calculateTransferFee_fail_invalid_amount() {
        // given
        Account account = Account.create("1234567890", "홍길동");

        // when & then
        assertThatThrownBy(() -> account.calculateTransferFee(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("금액은 0보다 커야 합니다");
    }

    @Test
    @DisplayName("계좌 삭제 - soft delete")
    void delete_soft_delete() {
        // given
        Account account = Account.create("1234567890", "홍길동");
        LocalDateTime deletedTime = LocalDateTime.of(2025, 12, 28, 10, 30);

        // when
        account.delete(deletedTime);

        // then
        assertThat(account.isDeleted()).isTrue();
        assertThat(account.getDeletedAt()).isEqualTo(deletedTime);
    }
}
