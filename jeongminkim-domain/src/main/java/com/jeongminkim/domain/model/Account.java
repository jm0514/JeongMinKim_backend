package com.jeongminkim.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 순수 도메인 모델 - 인프라 의존성 없음
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Account {

    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private String ownerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    /**
     * 계좌 생성 팩토리 메서드
     */
    public static Account create(String accountNumber, String ownerName, LocalDateTime now) {
        return Account.builder()
                .accountNumber(accountNumber)
                .ownerName(ownerName)
                .balance(BigDecimal.ZERO)
                .createdAt(now)
                .build();
    }

    /**
     * 입금
     */
    public Account deposit(BigDecimal amount, LocalDateTime now) {
        validateAmount(amount);
        return Account.builder()
                .id(this.id)
                .accountNumber(this.accountNumber)
                .ownerName(this.ownerName)
                .balance(this.balance.add(amount))
                .createdAt(this.createdAt)
                .updatedAt(now)
                .deletedAt(this.deletedAt)
                .build();
    }

    /**
     * 출금
     */
    public Account withdraw(BigDecimal amount, LocalDateTime now) {
        validateAmount(amount);
        if (!hasEnoughBalance(amount)) {
            throw new IllegalArgumentException(
                String.format("잔액이 부족합니다. (현재 잔액: %s원, 요청 금액: %s원)",
                    this.balance, amount)
            );
        }
        return Account.builder()
                .id(this.id)
                .accountNumber(this.accountNumber)
                .ownerName(this.ownerName)
                .balance(this.balance.subtract(amount))
                .createdAt(this.createdAt)
                .updatedAt(now)
                .deletedAt(this.deletedAt)
                .build();
    }

    /**
     * 잔액 확인
     */
    public boolean hasEnoughBalance(BigDecimal amount) {
        return this.balance.compareTo(amount) >= 0;
    }

    /**
     * 금액 검증
     */
    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("금액은 0보다 커야 합니다");
        }
    }

    /**
     * 계좌 삭제 (Soft Delete)
     */
    public Account delete(LocalDateTime deletedAt, LocalDateTime updatedAt) {
        return Account.builder()
                .id(this.id)
                .accountNumber(this.accountNumber)
                .ownerName(this.ownerName)
                .balance(this.balance)
                .createdAt(this.createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .build();
    }

    /**
     * 계좌 삭제 여부 확인
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * ID 설정 (영속화 후 사용)
     */
    public Account withId(Long id) {
        return Account.builder()
                .id(id)
                .accountNumber(this.accountNumber)
                .ownerName(this.ownerName)
                .balance(this.balance)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .deletedAt(this.deletedAt)
                .build();
    }

    /**
     * 타임스탬프 설정
     */
    public Account withTimestamps(LocalDateTime createdAt, LocalDateTime updatedAt) {
        return Account.builder()
                .id(this.id)
                .accountNumber(this.accountNumber)
                .ownerName(this.ownerName)
                .balance(this.balance)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(this.deletedAt)
                .build();
    }
}