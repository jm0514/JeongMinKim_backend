package com.jeongminkim.infrastructure.adapter.out.persistence;

import com.jeongminkim.domain.model.Account;
import com.jeongminkim.domain.port.out.AccountPort;
import com.jeongminkim.infrastructure.persistence.jpa.entity.AccountEntity;
import com.jeongminkim.infrastructure.persistence.jpa.mapper.AccountMapper;
import com.jeongminkim.infrastructure.persistence.jpa.repository.AccountJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Account Persistence Adapter
 * AccountPort 구현체 (Outbound Adapter)
 */
@Component
@RequiredArgsConstructor
public class AccountPersistenceAdapter implements AccountPort {

    private final AccountJpaRepository accountJpaRepository;
    private final AccountMapper accountMapper;

    @Override
    public Account save(Account account) {
        AccountEntity entity = accountMapper.toEntity(account);
        AccountEntity saved = accountJpaRepository.save(entity);
        return accountMapper.toDomain(saved);
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return accountJpaRepository.findByAccountNumber(accountNumber)
                .map(accountMapper::toDomain);
    }

    @Override
    public Optional<Account> findByAccountNumberWithLock(String accountNumber) {
        return accountJpaRepository.findByAccountNumberWithLock(accountNumber)
                .map(accountMapper::toDomain);
    }

    @Override
    public boolean existsByAccountNumber(String accountNumber) {
        return accountJpaRepository.existsByAccountNumberAndDeletedAtIsNull(accountNumber);
    }

    @Override
    public void delete(Account account) {
        AccountEntity entity = accountMapper.toEntity(account);
        accountJpaRepository.save(entity);
    }
}