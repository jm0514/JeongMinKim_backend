package com.jeongminkim.application.usecase;

import com.jeongminkim.application.service.policy.FeeCalculator;
import com.jeongminkim.application.service.policy.TransferLimitChecker;
import com.jeongminkim.application.service.policy.WithdrawalLimitChecker;
import com.jeongminkim.domain.exception.DomainException;
import com.jeongminkim.domain.exception.ErrorType;
import com.jeongminkim.domain.model.Account;
import com.jeongminkim.domain.model.Transaction;
import com.jeongminkim.domain.port.in.DepositUseCase;
import com.jeongminkim.domain.port.in.GetTransactionHistoryUseCase;
import com.jeongminkim.domain.port.in.TransferUseCase;
import com.jeongminkim.domain.port.in.WithdrawUseCase;
import com.jeongminkim.domain.port.out.AccountPort;
import com.jeongminkim.domain.port.out.TimePort;
import com.jeongminkim.domain.port.out.TransactionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 거래 Use Case 구현
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionUseCase implements
        DepositUseCase,
        WithdrawUseCase,
        TransferUseCase,
        GetTransactionHistoryUseCase {

    private final AccountPort accountPort;
    private final TransactionPort transactionPort;
    private final TimePort timePort;
    private final WithdrawalLimitChecker withdrawalLimitChecker;
    private final TransferLimitChecker transferLimitChecker;
    private final FeeCalculator feeCalculator;

    @Override
    @Transactional
    public Transaction deposit(String accountNumber, BigDecimal amount) {
        validateAmount(amount);

        // 계좌 조회
        Account account = findAccountByNumber(accountNumber);

        // 입금
        Account updatedAccount = account.deposit(amount, timePort.now());

        // 계좌 업데이트
        accountPort.save(updatedAccount);

        // 거래 내역 생성
        Transaction transaction = Transaction.createDeposit(
                updatedAccount.getId(),
                amount,
                updatedAccount.getBalance(),
                timePort.now()
        );

        return transactionPort.save(transaction);
    }

    @Override
    @Transactional
    public Transaction withdraw(String accountNumber, BigDecimal amount) {
        validateAmount(amount);

        // 계좌 조회 (락)
        Account account = accountPort.findByAccountNumberWithLock(accountNumber)
                .orElseThrow(() -> new DomainException(ErrorType.ACCOUNT_NOT_FOUND, accountNumber));

        // 한도 체크
        withdrawalLimitChecker.checkLimit(account.getId(), amount);

        // 출금
        Account updatedAccount = account.withdraw(amount, timePort.now());

        // 계좌 업데이트
        accountPort.save(updatedAccount);

        // 거래 내역 생성
        Transaction transaction = Transaction.createWithdrawal(
                updatedAccount.getId(),
                amount,
                updatedAccount.getBalance(),
                timePort.now()
        );

        return transactionPort.save(transaction);
    }

    @Override
    @Transactional
    public TransferResult transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        validateAmount(amount);

        // 동일 계좌 검증
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new DomainException(ErrorType.INVALID_TRANSFER, "동일한 계좌로 이체할 수 없습니다");
        }

        // 출금 계좌 조회 (락)
        Account fromAccount = accountPort.findByAccountNumberWithLock(fromAccountNumber)
                .orElseThrow(() -> new DomainException(ErrorType.ACCOUNT_NOT_FOUND, fromAccountNumber));

        // 입금 계좌 조회 (락)
        Account toAccount = accountPort.findByAccountNumberWithLock(toAccountNumber)
                .orElseThrow(() -> new DomainException(ErrorType.ACCOUNT_NOT_FOUND, toAccountNumber));

        // 수수료 계산
        BigDecimal fee = feeCalculator.calculate(amount);
        BigDecimal totalAmount = amount.add(fee);

        // 한도 체크
        transferLimitChecker.checkLimit(fromAccount.getId(), amount);

        // 잔액 확인 (금액 + 수수료)
        if (!fromAccount.hasEnoughBalance(totalAmount)) {
            throw new DomainException(
                    ErrorType.INSUFFICIENT_BALANCE,
                    String.format("현재 잔액: %s원, 이체 금액: %s원, 수수료: %s원",
                            fromAccount.getBalance(), amount, fee)
            );
        }

        // 출금 (금액 + 수수료)
        Account updatedFromAccount = fromAccount.withdraw(totalAmount, timePort.now());

        // 입금 (금액만)
        Account updatedToAccount = toAccount.deposit(amount, timePort.now());

        // 계좌 업데이트
        accountPort.save(updatedFromAccount);
        accountPort.save(updatedToAccount);

        // 출금 거래 내역
        Transaction fromTransaction = Transaction.createTransferOut(
                updatedFromAccount.getId(),
                amount,
                fee,
                updatedFromAccount.getBalance(),
                toAccountNumber,
                timePort.now()
        );

        // 입금 거래 내역
        Transaction toTransaction = Transaction.createTransferIn(
                updatedToAccount.getId(),
                amount,
                updatedToAccount.getBalance(),
                fromAccountNumber,
                timePort.now()
        );

        Transaction savedFromTx = transactionPort.save(fromTransaction);
        Transaction savedToTx = transactionPort.save(toTransaction);

        return new TransferResult(savedFromTx, savedToTx);
    }

    @Override
    public TransactionPage getTransactionHistory(String accountNumber, int page, int size) {
        // 계좌 조회
        Account account = findAccountByNumber(accountNumber);

        // 거래 내역 조회
        List<Transaction> transactions = transactionPort.findAllByAccountId(account.getId(), page, size);

        // 총 개수
        long totalElements = transactionPort.countByAccountId(account.getId());

        // 총 페이지
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return new TransactionPage(
                transactions,
                totalElements,
                totalPages,
                page,
                size
        );
    }

    private Account findAccountByNumber(String accountNumber) {
        return accountPort.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new DomainException(ErrorType.ACCOUNT_NOT_FOUND, accountNumber));
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException(ErrorType.INVALID_AMOUNT);
        }
    }
}