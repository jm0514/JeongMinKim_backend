package com.jeongminkim_backend.service;

import com.jeongminkim_backend.common.time.TimeProvider;
import com.jeongminkim_backend.domain.entity.Account;
import com.jeongminkim_backend.domain.entity.Transaction;
import com.jeongminkim_backend.domain.enums.TransactionType;
import com.jeongminkim_backend.dto.request.DepositRequest;
import com.jeongminkim_backend.dto.request.TransferRequest;
import com.jeongminkim_backend.dto.request.WithdrawRequest;
import com.jeongminkim_backend.dto.response.TransactionResponse;
import com.jeongminkim_backend.dto.response.TransferResponse;
import com.jeongminkim_backend.exception.BusinessException;
import com.jeongminkim_backend.exception.ErrorCode;
import com.jeongminkim_backend.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService 단위 테스트")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    @DisplayName("입금 성공")
    void deposit_success() {
        // given
        DepositRequest request = DepositRequest.builder()
                .accountNumber("1234567890")
                .amount(new BigDecimal("10000"))
                .build();

        Account account = Account.create("1234567890", "홍길동");

        Transaction savedTransaction = Transaction.createDeposit(
                1L,
                new BigDecimal("10000"),
                new BigDecimal("10000")
        );

        when(accountService.findAccountByAccountNumber("1234567890"))
                .thenReturn(account);
        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(savedTransaction);

        // when
        TransactionResponse response = transactionService.deposit(request);

        // then
        assertThat(response.getAccountNumber()).isEqualTo("1234567890");
        assertThat(response.getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(response.getAmount()).isEqualByComparingTo("10000");
        assertThat(response.getFee()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(accountService).findAccountByAccountNumber("1234567890");
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("입금 실패 - 존재하지 않는 계좌")
    void deposit_fail_accountNotFound() {
        // given
        DepositRequest request = DepositRequest.builder()
                .accountNumber("1234567890")
                .amount(new BigDecimal("10000"))
                .build();

        when(accountService.findAccountByAccountNumber("1234567890"))
                .thenThrow(new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, "1234567890"));

        // when & then
        assertThatThrownBy(() -> transactionService.deposit(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_NOT_FOUND);

        verify(accountService).findAccountByAccountNumber("1234567890");
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("출금 성공")
    void withdraw_success() {
        // given
        WithdrawRequest request = WithdrawRequest.builder()
                .accountNumber("1234567890")
                .amount(new BigDecimal("5000"))
                .build();

        Account account = Account.create("1234567890", "홍길동");
        account.deposit(new BigDecimal("50000"));

        LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);

        when(accountService.findAccountByAccountNumberWithLock("1234567890"))
                .thenReturn(account);
        when(timeProvider.now()).thenReturn(now);
        when(transactionRepository.sumAmountByAccountIdAndTypeAndDateRange(
                any(),
                eq(TransactionType.WITHDRAWAL),
                any(),
                any()
        )).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(Transaction.createWithdrawal(1L, new BigDecimal("5000"), new BigDecimal("45000")));

        // when
        TransactionResponse response = transactionService.withdraw(request);

        // then
        assertThat(response.getTransactionType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(response.getAmount()).isEqualByComparingTo("5000");
        assertThat(account.getBalance()).isEqualByComparingTo("45000");

        verify(accountService).findAccountByAccountNumberWithLock("1234567890");
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("출금 실패 - 일일 한도 초과")
    void withdraw_fail_dailyLimitExceeded() {
        // given
        WithdrawRequest request = WithdrawRequest.builder()
                .accountNumber("1234567890")
                .amount(new BigDecimal("200000"))
                .build();

        Account account = Account.create("1234567890", "홍길동");
        account.deposit(new BigDecimal("500000"));

        LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);

        when(accountService.findAccountByAccountNumberWithLock("1234567890"))
                .thenReturn(account);
        when(timeProvider.now()).thenReturn(now);
        when(transactionRepository.sumAmountByAccountIdAndTypeAndDateRange(
                any(),
                eq(TransactionType.WITHDRAWAL),
                eq(now.toLocalDate().atStartOfDay()),
                eq(now.toLocalDate().atTime(LocalTime.MAX))
        )).thenReturn(new BigDecimal("900000"));

        // when & then
        assertThatThrownBy(() -> transactionService.withdraw(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DAILY_WITHDRAWAL_LIMIT_EXCEEDED);

        verify(accountService).findAccountByAccountNumberWithLock("1234567890");
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("출금 성공 - 일일 한도 경계값")
    void withdraw_success_dailyLimitBoundary() {
        // given
        WithdrawRequest request = WithdrawRequest.builder()
                .accountNumber("1234567890")
                .amount(new BigDecimal("1000000"))
                .build();

        Account account = Account.create("1234567890", "홍길동");
        account.deposit(new BigDecimal("2000000"));

        LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);

        when(accountService.findAccountByAccountNumberWithLock("1234567890"))
                .thenReturn(account);
        when(timeProvider.now()).thenReturn(now);
        when(transactionRepository.sumAmountByAccountIdAndTypeAndDateRange(
                any(),
                eq(TransactionType.WITHDRAWAL),
                any(),
                any()
        )).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(Transaction.createWithdrawal(1L, new BigDecimal("1000000"), new BigDecimal("1000000")));

        // when
        TransactionResponse response = transactionService.withdraw(request);

        // then
        assertThat(response.getAmount()).isEqualByComparingTo("1000000");
        assertThat(account.getBalance()).isEqualByComparingTo("1000000");

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("이체 성공")
    void transfer_success() {
        // given
        TransferRequest request = TransferRequest.builder()
                .fromAccountNumber("1234567890")
                .toAccountNumber("0987654321")
                .amount(new BigDecimal("10000"))
                .build();

        Account fromAccount = Account.create("1234567890", "홍길동");
        fromAccount.deposit(new BigDecimal("50000"));

        Account toAccount = Account.create("0987654321", "김철수");

        LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);

        when(accountService.findAccountByAccountNumberWithLock("1234567890"))
                .thenReturn(fromAccount);
        when(accountService.findAccountByAccountNumberWithLock("0987654321"))
                .thenReturn(toAccount);
        when(timeProvider.now()).thenReturn(now);
        when(transactionRepository.sumAmountByAccountIdAndTypeAndDateRange(
                any(),
                eq(TransactionType.TRANSFER_OUT),
                any(),
                any()
        )).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        TransferResponse response = transactionService.transfer(request);

        // then
        assertThat(response.getFromTransaction().getTransactionType()).isEqualTo(TransactionType.TRANSFER_OUT);
        assertThat(response.getFromTransaction().getAmount()).isEqualByComparingTo("10000");
        assertThat(response.getFromTransaction().getFee()).isEqualByComparingTo("100");
        assertThat(response.getToTransaction().getTransactionType()).isEqualTo(TransactionType.TRANSFER_IN);
        assertThat(response.getToTransaction().getAmount()).isEqualByComparingTo("10000");
        assertThat(response.getToTransaction().getFee()).isEqualByComparingTo(BigDecimal.ZERO);

        assertThat(fromAccount.getBalance()).isEqualByComparingTo("39900");
        assertThat(toAccount.getBalance()).isEqualByComparingTo("10000");

        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("이체 실패 - 동일 계좌")
    void transfer_fail_sameAccount() {
        // given
        TransferRequest request = TransferRequest.builder()
                .fromAccountNumber("1234567890")
                .toAccountNumber("1234567890")
                .amount(new BigDecimal("10000"))
                .build();

        // when & then
        assertThatThrownBy(() -> transactionService.transfer(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_AMOUNT)
                .hasMessageContaining("동일한 계좌로 이체할 수 없습니다");

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("이체 실패 - 일일 이체 한도 초과")
    void transfer_fail_dailyLimitExceeded() {
        // given
        TransferRequest request = TransferRequest.builder()
                .fromAccountNumber("1234567890")
                .toAccountNumber("0987654321")
                .amount(new BigDecimal("600000"))
                .build();

        Account fromAccount = Account.create("1234567890", "홍길동");
        fromAccount.deposit(new BigDecimal("1000000"));

        Account toAccount = Account.create("0987654321", "김철수");

        LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);

        when(accountService.findAccountByAccountNumberWithLock("1234567890"))
                .thenReturn(fromAccount);
        when(accountService.findAccountByAccountNumberWithLock("0987654321"))
                .thenReturn(toAccount);
        when(timeProvider.now()).thenReturn(now);
        when(transactionRepository.sumAmountByAccountIdAndTypeAndDateRange(
                any(),
                eq(TransactionType.TRANSFER_OUT),
                any(),
                any()
        )).thenReturn(new BigDecimal("2500000"));

        // when & then
        assertThatThrownBy(() -> transactionService.transfer(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DAILY_TRANSFER_LIMIT_EXCEEDED);

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("거래 내역 조회 성공")
    void getTransactions_success() {
        // given
        String accountNumber = "1234567890";
        Account account = Account.create(accountNumber, "홍길동");

        Transaction tx1 = Transaction.createDeposit(1L, new BigDecimal("10000"), new BigDecimal("10000"));
        Transaction tx2 = Transaction.createWithdrawal(1L, new BigDecimal("5000"), new BigDecimal("5000"));

        Pageable pageable = PageRequest.of(0, 20);
        Page<Transaction> transactionPage = new PageImpl<>(List.of(tx1, tx2), pageable, 2);

        when(accountService.findAccountByAccountNumber(accountNumber))
                .thenReturn(account);
        when(transactionRepository.findByAccountIdOrderByCreatedAtDesc(any(), eq(pageable)))
                .thenReturn(transactionPage);

        // when
        Page<TransactionResponse> response = transactionService.getTransactions(accountNumber, pageable);

        // then
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent().get(0).getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(response.getContent().get(1).getTransactionType()).isEqualTo(TransactionType.WITHDRAWAL);

        verify(accountService).findAccountByAccountNumber(accountNumber);
        verify(transactionRepository).findByAccountIdOrderByCreatedAtDesc(any(), eq(pageable));
    }
}