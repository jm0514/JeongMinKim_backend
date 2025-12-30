package com.jeongminkim.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Account 도메인 모델 테스트")
class AccountTest {

    @Test
    @DisplayName("계좌 생성 시 잔액은 0이다")
    void create_account_with_zero_balance() {
        // when
        Account account = Account.create("1234567890", "홍길동");

        // then
        assertThat(account.getAccountNumber()).isEqualTo("1234567890");
        assertThat(account.getOwnerName()).isEqualTo("홍길동");
        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("입금 성공")
    void deposit_success() {
        // given
        Account account = Account.create("1234567890", "홍길동");

        // when
        Account updated = account.deposit(new BigDecimal("10000"));

        // then
        assertThat(updated.getBalance()).isEqualByComparingTo(new BigDecimal("10000"));
    }

    @Test
    @DisplayName("입금 실패 - 0원 이하 입금")
    void deposit_fail_zero_or_negative() {
        // given
        Account account = Account.create("1234567890", "홍길동");

        // when & then
        assertThatThrownBy(() -> account.deposit(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("금액은 0보다 커야 합니다");

        assertThatThrownBy(() -> account.deposit(new BigDecimal("-1000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("금액은 0보다 커야 합니다");
    }

    @Test
    @DisplayName("입금 실패 - null 금액")
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
        Account account = Account.create("1234567890", "홍길동")
                .deposit(new BigDecimal("50000"));

        // when
        Account updated = account.withdraw(new BigDecimal("30000"));

        // then
        assertThat(updated.getBalance()).isEqualByComparingTo(new BigDecimal("20000"));
    }

    @Test
    @DisplayName("출금 실패 - 잔액 부족")
    void withdraw_fail_insufficient_balance() {
        // given
        Account account = Account.create("1234567890", "홍길동")
                .deposit(new BigDecimal("10000"));

        // when & then
        assertThatThrownBy(() -> account.withdraw(new BigDecimal("20000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잔액이 부족합니다");
    }

    @Test
    @DisplayName("잔액 확인")
    void has_enough_balance() {
        // given
        Account account = Account.create("1234567890", "홍길동")
                .deposit(new BigDecimal("50000"));

        // when & then
        assertThat(account.hasEnoughBalance(new BigDecimal("30000"))).isTrue();
        assertThat(account.hasEnoughBalance(new BigDecimal("50000"))).isTrue();
        assertThat(account.hasEnoughBalance(new BigDecimal("60000"))).isFalse();
    }
}