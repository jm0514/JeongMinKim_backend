package com.jeongminkim_backend.domain.entity;

import com.jeongminkim_backend.domain.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", unique = true, nullable = false, length = 20)
    private String accountNumber;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(name = "owner_name", nullable = false, length = 100)
    private String ownerName;

    /**
     * 계좌 생성 팩토리 메서드
     */
    public static Account create(String accountNumber, String ownerName) {
        Account account = new Account();
        account.accountNumber = accountNumber;
        account.ownerName = ownerName;
        account.balance = BigDecimal.ZERO;
        return account;
    }

    /**
     * 입금
     * @param amount 입금 금액
     */
    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("입금 금액은 0보다 커야 합니다");
        }
        this.balance = this.balance.add(amount);
    }

    /**
     * 출금
     * @param amount 출금 금액
     */
    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("출금 금액은 0보다 커야 합니다");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("잔액이 부족합니다");
        }
        this.balance = this.balance.subtract(amount);
    }

    /**
     * 잔액 확인
     */
    public boolean hasEnoughBalance(BigDecimal amount) {
        return this.balance.compareTo(amount) >= 0;
    }
}
