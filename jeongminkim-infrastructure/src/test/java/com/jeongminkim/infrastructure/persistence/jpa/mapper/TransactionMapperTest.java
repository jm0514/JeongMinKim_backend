package com.jeongminkim.infrastructure.persistence.jpa.mapper;

import com.jeongminkim.domain.model.Transaction;
import com.jeongminkim.domain.model.TransactionType;
import com.jeongminkim.infrastructure.persistence.jpa.entity.TransactionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TransactionMapper 단위 테스트")
class TransactionMapperTest {

    private TransactionMapper transactionMapper;
    private final LocalDateTime fixedTime = LocalDateTime.of(2025, 12, 30, 10, 0);

    @BeforeEach
    void setUp() {
        transactionMapper = new TransactionMapper();
    }

    @Test
    @DisplayName("Domain → Entity 변환 성공")
    void toEntity_success() {
        // given - 가장 복잡한 케이스 (이체 출금: 수수료 + relatedAccountNumber 포함)
        Transaction transaction = Transaction.createTransferOut(
                1L,
                new BigDecimal("10000"),
                new BigDecimal("100"),
                new BigDecimal("39900"),
                "0987654321",
                fixedTime
        );

        // when
        TransactionEntity entity = transactionMapper.toEntity(transaction);

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getAccountId()).isEqualTo(1L);
        assertThat(entity.getTransactionType()).isEqualTo(TransactionType.TRANSFER_OUT);
        assertThat(entity.getAmount()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(entity.getFee()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(entity.getBalanceAfter()).isEqualByComparingTo(new BigDecimal("39900"));
        assertThat(entity.getRelatedAccountNumber()).isEqualTo("0987654321");
        assertThat(entity.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Entity → Domain 변환 성공")
    void toDomain_success() {
        // given - 가장 복잡한 케이스 (이체 출금: 수수료 + relatedAccountNumber 포함)
        LocalDateTime now = LocalDateTime.of(2025, 12, 30, 10, 0);
        TransactionEntity entity = TransactionEntity.builder()
                .id(1L)
                .accountId(1L)
                .transactionType(TransactionType.TRANSFER_OUT)
                .amount(new BigDecimal("10000"))
                .fee(new BigDecimal("100"))
                .balanceAfter(new BigDecimal("39900"))
                .relatedAccountNumber("0987654321")
                .createdAt(now)
                .build();

        // when
        Transaction transaction = transactionMapper.toDomain(entity);

        // then
        assertThat(transaction).isNotNull();
        assertThat(transaction.getId()).isEqualTo(1L);
        assertThat(transaction.getAccountId()).isEqualTo(1L);
        assertThat(transaction.getTransactionType()).isEqualTo(TransactionType.TRANSFER_OUT);
        assertThat(transaction.getAmount()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(transaction.getFee()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(transaction.getBalanceAfter()).isEqualByComparingTo(new BigDecimal("39900"));
        assertThat(transaction.getRelatedAccountNumber()).isEqualTo("0987654321");
        assertThat(transaction.getCreatedAt()).isEqualTo(now);
    }
}