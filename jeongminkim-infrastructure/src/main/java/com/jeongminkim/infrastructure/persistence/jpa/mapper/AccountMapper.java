package com.jeongminkim.infrastructure.persistence.jpa.mapper;

import com.jeongminkim.domain.model.Account;
import com.jeongminkim.infrastructure.persistence.jpa.entity.AccountEntity;
import org.springframework.stereotype.Component;

/**
 * Account Domain ↔ AccountEntity 매퍼
 */
@Component
public class AccountMapper {

    /**
     * Domain → Entity
     */
    public AccountEntity toEntity(Account account) {
        return AccountEntity.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .ownerName(account.getOwnerName())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .deletedAt(account.getDeletedAt())
                .build();
    }

    /**
     * Entity → Domain
     */
    public Account toDomain(AccountEntity entity) {
        return Account.builder()
                .id(entity.getId())
                .accountNumber(entity.getAccountNumber())
                .balance(entity.getBalance())
                .ownerName(entity.getOwnerName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }
}