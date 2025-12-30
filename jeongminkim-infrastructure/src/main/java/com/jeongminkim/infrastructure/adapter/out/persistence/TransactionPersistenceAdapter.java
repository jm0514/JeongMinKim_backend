package com.jeongminkim.infrastructure.adapter.out.persistence;

import com.jeongminkim.domain.model.Transaction;
import com.jeongminkim.domain.model.TransactionType;
import com.jeongminkim.domain.port.out.TransactionPort;
import com.jeongminkim.infrastructure.persistence.jpa.entity.TransactionEntity;
import com.jeongminkim.infrastructure.persistence.jpa.mapper.TransactionMapper;
import com.jeongminkim.infrastructure.persistence.jpa.repository.TransactionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Transaction Persistence Adapter
 * TransactionPort 구현체 (Outbound Adapter)
 */
@Component
@RequiredArgsConstructor
public class TransactionPersistenceAdapter implements TransactionPort {

    private final TransactionJpaRepository transactionJpaRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entity = transactionMapper.toEntity(transaction);
        TransactionEntity saved = transactionJpaRepository.save(entity);
        return transactionMapper.toDomain(saved);
    }

    @Override
    public List<Transaction> findAllByAccountId(Long accountId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TransactionEntity> entityPage = transactionJpaRepository.findAllByAccountIdOrderByCreatedAtDesc(accountId, pageRequest);
        return entityPage.getContent().stream()
                .map(transactionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal sumAmountByAccountIdAndTypeAndDate(Long accountId, TransactionType transactionType, LocalDate date) {
        LocalDateTime startDateTime = date.atStartOfDay();
        LocalDateTime endDateTime = date.plusDays(1).atStartOfDay();

        return transactionJpaRepository.sumAmountByAccountIdAndTypeAndDateRange(
                accountId,
                transactionType,
                startDateTime,
                endDateTime
        );
    }

    @Override
    public long countByAccountId(Long accountId) {
        return transactionJpaRepository.countByAccountId(accountId);
    }
}