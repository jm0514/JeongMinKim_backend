package com.jeongminkim_backend.domain.entity;

import com.jeongminkim_backend.domain.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends BaseTimeEntity {

    // 이체 수수료율: 1%
    private static final BigDecimal TRANSFER_FEE_RATE = new BigDecimal("0.01");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", unique = true, nullable = false, length = 20)
    private String accountNumber;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(name = "owner_name", nullable = false, length = 100)
    private String ownerName;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

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
        validateAmount(amount);
        this.balance = this.balance.add(amount);
    }

    /**
     * 출금
     * @param amount 출금 금액
     */
    public void withdraw(BigDecimal amount) {
        validateAmount(amount);
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

    /**
     * 이체 수수료 계산
     * @param amount 이체 금액
     * @return 수수료 (이체 금액의 1%, 소수점 둘째자리 반올림)
     */
    public BigDecimal calculateTransferFee(BigDecimal amount) {
        validateAmount(amount);
        return amount.multiply(TRANSFER_FEE_RATE)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 금액 검증
     * @param amount 검증할 금액
     */
    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("금액은 0보다 커야 합니다");
        }
    }

    /**
     * 계좌 삭제 (Soft Delete)
     * @param deletedAt 삭제 시간
     */
    public void delete(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    /**
     * 계좌 삭제 여부 확인
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
