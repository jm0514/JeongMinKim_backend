package com.jeongminkim.application.service;

import com.jeongminkim.core.domain.entity.Account;
import com.jeongminkim.core.exception.BusinessException;
import com.jeongminkim.core.exception.ErrorCode;
import com.jeongminkim.core.repository.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountReader 단위 테스트")
class AccountReaderTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountReader accountReader;

    @Test
    @DisplayName("계좌 조회 성공 - 락 없음")
    void findByAccountNumber_success() {
        // given
        String accountNumber = "1234567890";
        Account account = Account.create(accountNumber, "홍길동");

        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(account));

        // when
        Account result = accountReader.findByAccountNumber(accountNumber);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getAccountNumber()).isEqualTo(accountNumber);
        assertThat(result.getOwnerName()).isEqualTo("홍길동");

        verify(accountRepository).findByAccountNumber(accountNumber);
    }

    @Test
    @DisplayName("계좌 조회 실패 - 존재하지 않는 계좌 (락 없음)")
    void findByAccountNumber_fail_not_found() {
        // given
        String accountNumber = "9999999999";

        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountReader.findByAccountNumber(accountNumber))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_NOT_FOUND)
                .hasMessageContaining(accountNumber);

        verify(accountRepository).findByAccountNumber(accountNumber);
    }

    @Test
    @DisplayName("계좌 조회 성공 - 비관적 락")
    void findByAccountNumberWithLock_success() {
        // given
        String accountNumber = "1234567890";
        Account account = Account.create(accountNumber, "홍길동");

        when(accountRepository.findByAccountNumberWithLock(accountNumber))
                .thenReturn(Optional.of(account));

        // when
        Account result = accountReader.findByAccountNumberWithLock(accountNumber);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getAccountNumber()).isEqualTo(accountNumber);
        assertThat(result.getOwnerName()).isEqualTo("홍길동");

        verify(accountRepository).findByAccountNumberWithLock(accountNumber);
    }

    @Test
    @DisplayName("계좌 조회 실패 - 존재하지 않는 계좌 (비관적 락)")
    void findByAccountNumberWithLock_fail_not_found() {
        // given
        String accountNumber = "9999999999";

        when(accountRepository.findByAccountNumberWithLock(accountNumber))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountReader.findByAccountNumberWithLock(accountNumber))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_NOT_FOUND)
                .hasMessageContaining(accountNumber);

        verify(accountRepository).findByAccountNumberWithLock(accountNumber);
    }
}