package com.jeongminkim.application.usecase;

import com.jeongminkim.domain.exception.DomainException;
import com.jeongminkim.domain.exception.ErrorType;
import com.jeongminkim.domain.model.Account;
import com.jeongminkim.domain.port.out.AccountPort;
import com.jeongminkim.domain.port.out.TimePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountUseCase 테스트")
class AccountUseCaseTest {

    @Mock
    private AccountPort accountPort;

    @Mock
    private TimePort timePort;

    @InjectMocks
    private AccountUseCase accountUseCase;

    @Test
    @DisplayName("계좌 생성 성공")
    void createAccount_success() {
        // given
        LocalDateTime now = LocalDateTime.of(2025, 12, 30, 10, 0);
        Account account = Account.create("1234567890", "홍길동", now);

        when(accountPort.existsByAccountNumber("1234567890")).thenReturn(false);
        when(timePort.now()).thenReturn(now);
        when(accountPort.save(any(Account.class))).thenReturn(account);

        // when
        Account result = accountUseCase.createAccount("1234567890", "홍길동");

        // then
        assertThat(result.getAccountNumber()).isEqualTo("1234567890");
        assertThat(result.getOwnerName()).isEqualTo("홍길동");

        verify(accountPort).existsByAccountNumber("1234567890");
        verify(accountPort).save(any(Account.class));
    }

    @Test
    @DisplayName("계좌 생성 실패 - 중복 계좌번호")
    void createAccount_fail_duplicate() {
        // given
        when(accountPort.existsByAccountNumber("1234567890")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> accountUseCase.createAccount("1234567890", "홍길동"))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.DUPLICATE_ACCOUNT);

        verify(accountPort).existsByAccountNumber("1234567890");
        verify(accountPort, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("계좌 조회 성공")
    void getAccount_success() {
        // given
        LocalDateTime now = LocalDateTime.of(2025, 12, 30, 10, 0);
        Account account = Account.create("1234567890", "홍길동", now);

        when(accountPort.findByAccountNumber("1234567890")).thenReturn(Optional.of(account));

        // when
        Account result = accountUseCase.getAccount("1234567890");

        // then
        assertThat(result.getAccountNumber()).isEqualTo("1234567890");

        verify(accountPort).findByAccountNumber("1234567890");
    }

    @Test
    @DisplayName("계좌 조회 실패 - 존재하지 않는 계좌")
    void getAccount_fail_notFound() {
        // given
        when(accountPort.findByAccountNumber("1234567890")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountUseCase.getAccount("1234567890"))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.ACCOUNT_NOT_FOUND);

        verify(accountPort).findByAccountNumber("1234567890");
    }

    @Test
    @DisplayName("계좌 삭제 성공")
    void deleteAccount_success() {
        // given
        LocalDateTime now = LocalDateTime.of(2025, 12, 30, 10, 0);
        Account account = Account.create("1234567890", "홍길동", now);

        when(accountPort.findByAccountNumber("1234567890")).thenReturn(Optional.of(account));
        when(timePort.now()).thenReturn(now);
        when(accountPort.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        accountUseCase.deleteAccount("1234567890");

        // then
        verify(accountPort).findByAccountNumber("1234567890");
        verify(timePort, times(2)).now();  // deletedAt과 updatedAt 모두 now() 호출
        verify(accountPort).save(any(Account.class));
    }

    @Test
    @DisplayName("계좌 삭제 실패 - 존재하지 않는 계좌")
    void deleteAccount_fail_notFound() {
        // given
        when(accountPort.findByAccountNumber("1234567890")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountUseCase.deleteAccount("1234567890"))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.ACCOUNT_NOT_FOUND);

        verify(accountPort).findByAccountNumber("1234567890");
        verify(accountPort, never()).save(any());
    }
}
