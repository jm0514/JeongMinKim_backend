package com.jeongminkim.infrastructure.persistence.jpa.mapper;

import com.jeongminkim.domain.model.Account;
import com.jeongminkim.infrastructure.persistence.jpa.entity.AccountEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AccountMapper 단위 테스트")
class AccountMapperTest {

    private AccountMapper accountMapper;

    @BeforeEach
    void setUp() {
        accountMapper = new AccountMapper();
    }

    @Test
    @DisplayName("Domain → Entity 변환 성공")
    void toEntity_success() {
        // given
        LocalDateTime now = LocalDateTime.of(2025, 12, 30, 10, 0);
        LocalDateTime deletedAt = LocalDateTime.of(2025, 12, 30, 15, 0);
        Account account = Account.builder()
                .id(1L)
                .accountNumber("1234567890")
                .ownerName("홍길동")
                .balance(new BigDecimal("50000"))
                .createdAt(now)
                .updatedAt(now)
                .deletedAt(deletedAt)
                .build();

        // when
        AccountEntity entity = accountMapper.toEntity(account);

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getAccountNumber()).isEqualTo("1234567890");
        assertThat(entity.getOwnerName()).isEqualTo("홍길동");
        assertThat(entity.getBalance()).isEqualByComparingTo(new BigDecimal("50000"));
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
        assertThat(entity.getDeletedAt()).isEqualTo(deletedAt);
    }

    @Test
    @DisplayName("Entity → Domain 변환 성공")
    void toDomain_success() {
        // given
        LocalDateTime now = LocalDateTime.of(2025, 12, 30, 10, 0);
        LocalDateTime deletedAt = LocalDateTime.of(2025, 12, 30, 15, 0);
        AccountEntity entity = AccountEntity.builder()
                .id(1L)
                .accountNumber("1234567890")
                .ownerName("홍길동")
                .balance(new BigDecimal("50000"))
                .createdAt(now)
                .updatedAt(deletedAt)
                .deletedAt(deletedAt)
                .build();

        // when
        Account account = accountMapper.toDomain(entity);

        // then
        assertThat(account).isNotNull();
        assertThat(account.getId()).isEqualTo(1L);
        assertThat(account.getAccountNumber()).isEqualTo("1234567890");
        assertThat(account.getOwnerName()).isEqualTo("홍길동");
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("50000"));
        assertThat(account.getCreatedAt()).isEqualTo(now);
        assertThat(account.getUpdatedAt()).isEqualTo(deletedAt);
        assertThat(account.getDeletedAt()).isEqualTo(deletedAt);
    }
}