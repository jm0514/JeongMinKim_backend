package com.jeongminkim.application.usecase;

import com.jeongminkim.application.port.in.TransferUseCase;
import com.jeongminkim.application.service.policy.FeeCalculator;
import com.jeongminkim.application.service.policy.TransferLimitChecker;
import com.jeongminkim.application.service.policy.WithdrawalLimitChecker;
import com.jeongminkim.domain.exception.DomainException;
import com.jeongminkim.domain.exception.ErrorType;
import com.jeongminkim.domain.model.Account;
import com.jeongminkim.domain.model.Transaction;
import com.jeongminkim.domain.port.out.AccountPort;
import com.jeongminkim.domain.port.out.TimePort;
import com.jeongminkim.domain.port.out.TransactionPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionUseCase 테스트")
class TransactionUseCaseTest {

    @Mock
    private AccountPort accountPort;

    @Mock
    private TransactionPort transactionPort;

    @Mock
    private WithdrawalLimitChecker withdrawalLimitChecker;

    @Mock
    private TransferLimitChecker transferLimitChecker;

    @Mock
    private FeeCalculator feeCalculator;

    @Mock
    private TimePort timePort;

    @InjectMocks
    private TransactionUseCase transactionUseCase;

    private final LocalDateTime fixedTime = LocalDateTime.of(2025, 12, 30, 10, 0);

    @Test
    @DisplayName("입금 성공")
    void deposit_success() {
        // given
        Account account = Account.create("1234567890", "홍길동", fixedTime);
        Account updatedAccount = account.deposit(new BigDecimal("10000"), fixedTime);

        when(accountPort.findByAccountNumber("1234567890")).thenReturn(Optional.of(account));
        when(timePort.now()).thenReturn(fixedTime);
        when(accountPort.save(any(Account.class))).thenReturn(updatedAccount);
        when(transactionPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Transaction result = transactionUseCase.deposit("1234567890", new BigDecimal("10000"));

        // then
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("10000"));

        verify(accountPort).findByAccountNumber("1234567890");
        verify(accountPort).save(any(Account.class));
        verify(transactionPort).save(any(Transaction.class));
    }

    @Test
    @DisplayName("입금 실패 - 유효하지 않은 금액")
    void deposit_fail_invalid_amount() {
        // when & then
        assertThatThrownBy(() -> transactionUseCase.deposit("1234567890", BigDecimal.ZERO))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_AMOUNT);

        verify(accountPort, never()).findByAccountNumber(any());
    }

    @Test
    @DisplayName("출금 성공")
    void withdraw_success() {
        // given
        Account account = Account.create("1234567890", "홍길동", fixedTime)
                .withId(1L)
                .deposit(new BigDecimal("50000"), fixedTime);
        Account updatedAccount = account.withdraw(new BigDecimal("30000"), fixedTime);

        when(accountPort.findByAccountNumberWithLock("1234567890")).thenReturn(Optional.of(account));
        when(timePort.now()).thenReturn(fixedTime);
        when(accountPort.save(any(Account.class))).thenReturn(updatedAccount);
        when(transactionPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(withdrawalLimitChecker).checkLimit(any(), any());

        // when
        Transaction result = transactionUseCase.withdraw("1234567890", new BigDecimal("30000"));

        // then
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("30000"));

        verify(accountPort).findByAccountNumberWithLock("1234567890");
        verify(withdrawalLimitChecker).checkLimit(any(), any());
        verify(accountPort).save(any(Account.class));
        verify(transactionPort).save(any(Transaction.class));
    }

    @Test
    @DisplayName("입금 실패 - 존재하지 않는 계좌")
    void deposit_fail_accountNotFound() {
        // given
        when(accountPort.findByAccountNumber("1234567890")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> transactionUseCase.deposit("1234567890", new BigDecimal("10000")))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.ACCOUNT_NOT_FOUND);

        verify(accountPort).findByAccountNumber("1234567890");
        verify(transactionPort, never()).save(any());
    }

    @Test
    @DisplayName("출금 실패 - 잔액 부족")
    void withdraw_fail_insufficient_balance() {
        // given
        Account account = Account.create("1234567890", "홍길동", fixedTime)
                .withId(1L)
                .deposit(new BigDecimal("10000"), fixedTime);

        when(accountPort.findByAccountNumberWithLock("1234567890")).thenReturn(Optional.of(account));
        doNothing().when(withdrawalLimitChecker).checkLimit(any(), any());

        // when & then
        assertThatThrownBy(() -> transactionUseCase.withdraw("1234567890", new BigDecimal("20000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잔액이 부족합니다");

        verify(accountPort).findByAccountNumberWithLock("1234567890");
        verify(withdrawalLimitChecker).checkLimit(any(), any());
        verify(accountPort, never()).save(any());
    }

    @Test
    @DisplayName("출금 실패 - 일일 한도 초과")
    void withdraw_fail_dailyLimitExceeded() {
        // given
        Account account = Account.create("1234567890", "홍길동", fixedTime)
                .withId(1L)
                .deposit(new BigDecimal("500000"), fixedTime);

        when(accountPort.findByAccountNumberWithLock("1234567890")).thenReturn(Optional.of(account));
        doThrow(new DomainException(ErrorType.DAILY_WITHDRAWAL_LIMIT_EXCEEDED))
                .when(withdrawalLimitChecker).checkLimit(any(), any());

        // when & then
        assertThatThrownBy(() -> transactionUseCase.withdraw("1234567890", new BigDecimal("200000")))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.DAILY_WITHDRAWAL_LIMIT_EXCEEDED);

        verify(accountPort).findByAccountNumberWithLock("1234567890");
        verify(withdrawalLimitChecker).checkLimit(any(), any());
        verify(transactionPort, never()).save(any());
    }

    @Test
    @DisplayName("출금 성공 - 일일 한도 경계값")
    void withdraw_success_dailyLimitBoundary() {
        // given
        Account account = Account.create("1234567890", "홍길동", fixedTime)
                .withId(1L)
                .deposit(new BigDecimal("2000000"), fixedTime);
        Account updatedAccount = account.withdraw(new BigDecimal("1000000"), fixedTime);

        when(accountPort.findByAccountNumberWithLock("1234567890")).thenReturn(Optional.of(account));
        when(timePort.now()).thenReturn(fixedTime);
        when(accountPort.save(any(Account.class))).thenReturn(updatedAccount);
        when(transactionPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(withdrawalLimitChecker).checkLimit(any(), any());

        // when
        Transaction result = transactionUseCase.withdraw("1234567890", new BigDecimal("1000000"));

        // then
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("1000000"));

        verify(accountPort).findByAccountNumberWithLock("1234567890");
        verify(withdrawalLimitChecker).checkLimit(any(), any());
        verify(transactionPort).save(any(Transaction.class));
    }

    @Test
    @DisplayName("이체 성공")
    void transfer_success() {
        // given
        Account fromAccount = Account.create("1234567890", "홍길동", fixedTime)
                .withId(1L)
                .deposit(new BigDecimal("50000"), fixedTime);
        Account toAccount = Account.create("0987654321", "김철수", fixedTime)
                .withId(2L);

        BigDecimal amount = new BigDecimal("10000");
        BigDecimal fee = new BigDecimal("100");

        when(accountPort.findByAccountNumberWithLock("1234567890")).thenReturn(Optional.of(fromAccount));
        when(accountPort.findByAccountNumberWithLock("0987654321")).thenReturn(Optional.of(toAccount));
        when(feeCalculator.calculate(any())).thenReturn(fee);
        when(timePort.now()).thenReturn(fixedTime);
        doNothing().when(transferLimitChecker).checkLimit(any(), any());
        when(accountPort.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> {
                    Transaction tx = invocation.getArgument(0);
                    return tx.withId(tx.getAccountId());
                });

        // when
        TransferUseCase.TransferResult result = transactionUseCase.transfer("1234567890", "0987654321", amount);

        // then
        assertThat(result.fromTransaction().getAmount()).isEqualByComparingTo(amount);
        assertThat(result.fromTransaction().getFee()).isEqualByComparingTo(fee);
        assertThat(result.toTransaction().getAmount()).isEqualByComparingTo(amount);
        assertThat(result.toTransaction().getFee()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(accountPort).findByAccountNumberWithLock("1234567890");
        verify(accountPort).findByAccountNumberWithLock("0987654321");
        verify(feeCalculator).calculate(any());
        verify(transferLimitChecker).checkLimit(any(), any());
        verify(transactionPort, times(2)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("이체 실패 - 동일 계좌")
    void transfer_fail_sameAccount() {
        // when & then
        assertThatThrownBy(() -> transactionUseCase.transfer("1234567890", "1234567890", new BigDecimal("10000")))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_TRANSFER)
                .hasMessageContaining("동일한 계좌로 이체할 수 없습니다");

        verify(transactionPort, never()).save(any());
    }

    @Test
    @DisplayName("이체 실패 - 잔액 부족 (수수료 포함)")
    void transfer_fail_insufficientBalance() {
        // given
        Account fromAccount = Account.create("1234567890", "홍길동", fixedTime)
                .withId(1L)
                .deposit(new BigDecimal("10000"), fixedTime);
        Account toAccount = Account.create("0987654321", "김철수", fixedTime)
                .withId(2L);

        BigDecimal amount = new BigDecimal("10000");
        BigDecimal fee = new BigDecimal("100");

        when(accountPort.findByAccountNumberWithLock("1234567890")).thenReturn(Optional.of(fromAccount));
        when(accountPort.findByAccountNumberWithLock("0987654321")).thenReturn(Optional.of(toAccount));
        when(feeCalculator.calculate(any())).thenReturn(fee);
        doNothing().when(transferLimitChecker).checkLimit(any(), any());

        // when & then
        assertThatThrownBy(() -> transactionUseCase.transfer("1234567890", "0987654321", amount))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INSUFFICIENT_BALANCE)
                .hasMessageContaining("현재 잔액: 10000");

        verify(accountPort).findByAccountNumberWithLock("1234567890");
        verify(accountPort).findByAccountNumberWithLock("0987654321");
        verify(feeCalculator).calculate(any());
        verify(transferLimitChecker).checkLimit(any(), any());
        verify(transactionPort, never()).save(any());
    }

    @Test
    @DisplayName("이체 실패 - 일일 이체 한도 초과")
    void transfer_fail_dailyLimitExceeded() {
        // given
        Account fromAccount = Account.create("1234567890", "홍길동", fixedTime)
                .withId(1L)
                .deposit(new BigDecimal("1000000"), fixedTime);
        Account toAccount = Account.create("0987654321", "김철수", fixedTime)
                .withId(2L);

        BigDecimal amount = new BigDecimal("600000");

        when(accountPort.findByAccountNumberWithLock("1234567890")).thenReturn(Optional.of(fromAccount));
        when(accountPort.findByAccountNumberWithLock("0987654321")).thenReturn(Optional.of(toAccount));
        when(feeCalculator.calculate(any())).thenReturn(new BigDecimal("6000"));
        doThrow(new DomainException(ErrorType.DAILY_TRANSFER_LIMIT_EXCEEDED))
                .when(transferLimitChecker).checkLimit(any(), any());

        // when & then
        assertThatThrownBy(() -> transactionUseCase.transfer("1234567890", "0987654321", amount))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.DAILY_TRANSFER_LIMIT_EXCEEDED);

        verify(accountPort).findByAccountNumberWithLock("1234567890");
        verify(accountPort).findByAccountNumberWithLock("0987654321");
        verify(transferLimitChecker).checkLimit(any(), any());
        verify(transactionPort, never()).save(any());
    }

    @Test
    @DisplayName("거래 내역 조회 성공")
    void getTransactions_success() {
        // given
        String accountNumber = "1234567890";
        Account account = Account.create(accountNumber, "홍길동", fixedTime).withId(1L);

        Transaction tx1 = Transaction.createDeposit(1L, new BigDecimal("10000"), new BigDecimal("10000"), fixedTime);
        Transaction tx2 = Transaction.createWithdrawal(1L, new BigDecimal("5000"), new BigDecimal("5000"), fixedTime);

        when(accountPort.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));
        when(transactionPort.findAllByAccountId(eq(1L), eq(0), eq(20)))
                .thenReturn(List.of(tx1, tx2));
        when(transactionPort.countByAccountId(1L)).thenReturn(2L);

        // when
        var result = transactionUseCase.getTransactionHistory(accountNumber, 0, 20);

        // then
        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.transactions()).hasSize(2);
        assertThat(result.transactions().get(0).getAmount()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(result.transactions().get(1).getAmount()).isEqualByComparingTo(new BigDecimal("5000"));

        verify(accountPort).findByAccountNumber(accountNumber);
        verify(transactionPort).findAllByAccountId(1L, 0, 20);
        verify(transactionPort).countByAccountId(1L);
    }
}