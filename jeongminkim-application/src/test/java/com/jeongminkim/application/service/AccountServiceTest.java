package com.jeongminkim.application.service;

import com.jeongminkim.application.common.time.TimeProvider;
import com.jeongminkim.core.domain.entity.Account;
import com.jeongminkim.application.dto.request.CreateAccountRequest;
import com.jeongminkim.application.dto.response.AccountResponse;
import com.jeongminkim.core.exception.BusinessException;
import com.jeongminkim.core.exception.ErrorCode;
import com.jeongminkim.core.repository.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService 단위 테스트")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountReader accountReader;

    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("계좌 생성 성공")
    void createAccount_success() {
        // given
        CreateAccountRequest request = CreateAccountRequest.builder()
                .accountNumber("1234567890")
                .ownerName("홍길동")
                .build();

        Account savedAccount = Account.create("1234567890", "홍길동");

        when(accountRepository.existsByAccountNumberAndDeletedAtIsNull("1234567890"))
                .thenReturn(false);
        when(accountRepository.save(any(Account.class)))
                .thenReturn(savedAccount);

        // when
        AccountResponse response = accountService.createAccount(request);

        // then
        assertThat(response.getAccountNumber()).isEqualTo("1234567890");
        assertThat(response.getOwnerName()).isEqualTo("홍길동");
        assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(accountRepository).existsByAccountNumberAndDeletedAtIsNull("1234567890");
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("계좌 생성 실패 - 중복 계좌번호")
    void createAccount_fail_duplicate() {
        // given
        CreateAccountRequest request = CreateAccountRequest.builder()
                .accountNumber("1234567890")
                .ownerName("홍길동")
                .build();

        when(accountRepository.existsByAccountNumberAndDeletedAtIsNull("1234567890"))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_ACCOUNT);

        verify(accountRepository).existsByAccountNumberAndDeletedAtIsNull("1234567890");
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("계좌 조회 성공 - Response 반환")
    void getAccount_success() {
        // given
        Account account = Account.create("1234567890", "홍길동");
        account.deposit(new BigDecimal("50000"));

        when(accountReader.findByAccountNumber("1234567890"))
                .thenReturn(account);

        // when
        AccountResponse response = accountService.getAccount("1234567890");

        // then
        assertThat(response.getAccountNumber()).isEqualTo("1234567890");
        assertThat(response.getOwnerName()).isEqualTo("홍길동");
        assertThat(response.getBalance()).isEqualByComparingTo("50000");

        verify(accountReader).findByAccountNumber("1234567890");
    }

    @Test
    @DisplayName("계좌 조회 실패 - 존재하지 않는 계좌")
    void getAccount_fail_notFound() {
        // given
        when(accountReader.findByAccountNumber("1234567890"))
                .thenThrow(new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, "1234567890"));

        // when & then
        assertThatThrownBy(() -> accountService.getAccount("1234567890"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_NOT_FOUND);

        verify(accountReader).findByAccountNumber("1234567890");
    }

    @Test
    @DisplayName("계좌 삭제 성공 - Soft Delete")
    void deleteAccount_success() {
        // given
        String accountNumber = "1234567890";
        LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 15, 10, 30);

        Account account = Account.create(accountNumber, "홍길동");

        when(accountReader.findByAccountNumber(accountNumber))
                .thenReturn(account);
        when(timeProvider.now())
                .thenReturn(fixedTime);

        // when
        accountService.deleteAccount(accountNumber);

        // then
        assertThat(account.getDeletedAt()).isEqualTo(fixedTime);
        assertThat(account.isDeleted()).isTrue();

        verify(accountReader).findByAccountNumber(accountNumber);
        verify(timeProvider).now();
    }

    @Test
    @DisplayName("계좌 삭제 실패 - 존재하지 않는 계좌")
    void deleteAccount_fail_notFound() {
        // given
        when(accountReader.findByAccountNumber("1234567890"))
                .thenThrow(new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, "1234567890"));

        // when & then
        assertThatThrownBy(() -> accountService.deleteAccount("1234567890"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_NOT_FOUND);

        verify(accountReader).findByAccountNumber("1234567890");
    }
}
