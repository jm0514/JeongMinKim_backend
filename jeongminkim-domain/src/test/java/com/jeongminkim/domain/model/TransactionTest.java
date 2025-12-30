package com.jeongminkim.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Transaction 도메인 모델 테스트")
class TransactionTest {

    private final LocalDateTime fixedTime = LocalDateTime.of(2025, 12, 30, 10, 0);

    @Test
    @DisplayName("입금 거래 생성 - 잔액 0원인 계좌에 10,000원 입금")
    void createDeposit() {
        // given
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("10000");
        BigDecimal balanceAfter = new BigDecimal("10000");

        // when
        Transaction transaction = Transaction.createDeposit(accountId, amount, balanceAfter, fixedTime);

        // then
        assertThat(transaction.getAccountId()).isEqualTo(accountId);
        assertThat(transaction.getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(transaction.getAmount()).isEqualByComparingTo(amount);
        assertThat(transaction.getFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(transaction.getBalanceAfter()).isEqualByComparingTo(balanceAfter);
        assertThat(transaction.getRelatedAccountNumber()).isNull();
        assertThat(transaction.getCreatedAt()).isEqualTo(fixedTime);
    }

    @Test
    @DisplayName("출금 거래 생성 - 잔액 10,000원인 계좌에서 5,000원 출금")
    void createWithdrawal() {
        // given
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("5000");
        BigDecimal balanceAfter = new BigDecimal("5000");

        // when
        Transaction transaction = Transaction.createWithdrawal(accountId, amount, balanceAfter, fixedTime);

        // then
        assertThat(transaction.getAccountId()).isEqualTo(accountId);
        assertThat(transaction.getTransactionType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(transaction.getAmount()).isEqualByComparingTo(amount);
        assertThat(transaction.getFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(transaction.getBalanceAfter()).isEqualByComparingTo(balanceAfter);
        assertThat(transaction.getRelatedAccountNumber()).isNull();
        assertThat(transaction.getCreatedAt()).isEqualTo(fixedTime);
    }

    @Test
    @DisplayName("이체 출금 거래 생성 - 잔액 50,000원인 계좌에서 20,000원 이체 (수수료 1%)")
    void createTransferOut() {
        // given
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("20000");
        BigDecimal fee = new BigDecimal("200");
        BigDecimal balanceAfter = new BigDecimal("29800");
        String toAccountNumber = "9876543210";

        // when
        Transaction transaction = Transaction.createTransferOut(
                accountId, amount, fee, balanceAfter, toAccountNumber, fixedTime
        );

        // then
        assertThat(transaction.getAccountId()).isEqualTo(accountId);
        assertThat(transaction.getTransactionType()).isEqualTo(TransactionType.TRANSFER_OUT);
        assertThat(transaction.getAmount()).isEqualByComparingTo(amount);
        assertThat(transaction.getFee()).isEqualByComparingTo(fee);
        assertThat(transaction.getBalanceAfter()).isEqualByComparingTo(balanceAfter);
        assertThat(transaction.getRelatedAccountNumber()).isEqualTo(toAccountNumber);
        assertThat(transaction.getCreatedAt()).isEqualTo(fixedTime);
    }

    @Test
    @DisplayName("이체 입금 거래 생성 - 잔액 0원인 계좌가 20,000원 이체받음")
    void createTransferIn() {
        // given
        Long accountId = 2L;
        BigDecimal amount = new BigDecimal("20000");
        BigDecimal balanceAfter = new BigDecimal("20000");
        String fromAccountNumber = "1234567890";

        // when
        Transaction transaction = Transaction.createTransferIn(
                accountId, amount, balanceAfter, fromAccountNumber, fixedTime
        );

        // then
        assertThat(transaction.getAccountId()).isEqualTo(accountId);
        assertThat(transaction.getTransactionType()).isEqualTo(TransactionType.TRANSFER_IN);
        assertThat(transaction.getAmount()).isEqualByComparingTo(amount);
        assertThat(transaction.getFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(transaction.getBalanceAfter()).isEqualByComparingTo(balanceAfter);
        assertThat(transaction.getRelatedAccountNumber()).isEqualTo(fromAccountNumber);
        assertThat(transaction.getCreatedAt()).isEqualTo(fixedTime);
    }
}