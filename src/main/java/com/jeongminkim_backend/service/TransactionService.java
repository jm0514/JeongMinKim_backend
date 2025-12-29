package com.jeongminkim_backend.service;

import com.jeongminkim_backend.common.time.TimeProvider;
import com.jeongminkim_backend.domain.entity.Account;
import com.jeongminkim_backend.domain.entity.Transaction;
import com.jeongminkim_backend.dto.request.DepositRequest;
import com.jeongminkim_backend.dto.request.TransferRequest;
import com.jeongminkim_backend.dto.request.WithdrawRequest;
import com.jeongminkim_backend.dto.response.TransactionResponse;
import com.jeongminkim_backend.dto.response.TransferResponse;
import com.jeongminkim_backend.exception.BusinessException;
import com.jeongminkim_backend.exception.ErrorCode;
import com.jeongminkim_backend.repository.TransactionRepository;
import com.jeongminkim_backend.service.policy.FeeCalculator;
import com.jeongminkim_backend.service.policy.WithdrawalLimitChecker;
import com.jeongminkim_backend.service.policy.TransferLimitChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountReader accountReader;
    private final FeeCalculator feeCalculator;
    private final WithdrawalLimitChecker withdrawalLimitChecker;
    private final TransferLimitChecker transferLimitChecker;
    private final TimeProvider timeProvider;

    /**
     * 입금
     */
    @Transactional
    public TransactionResponse deposit(DepositRequest request) {
        log.info("입금 시작: 계좌={}, 금액={}", request.getAccountNumber(), request.getAmount());

        Account account = accountReader.findByAccountNumber(request.getAccountNumber());

        account.deposit(request.getAmount());

        Transaction transaction = Transaction.createDeposit(
                account.getId(),
                request.getAmount(),
                account.getBalance()
        );
        transactionRepository.save(transaction);

        log.info("입금 완료: 계좌={}, 금액={}, 잔액={}", 
                request.getAccountNumber(), request.getAmount(), account.getBalance());

        return TransactionResponse.from(transaction, account.getAccountNumber());
    }

    /**
     * 출금
     */
    @Transactional
    public TransactionResponse withdraw(WithdrawRequest request) {
        log.info("출금 시작: 계좌={}, 금액={}", request.getAccountNumber(), request.getAmount());

        Account account = accountReader.findByAccountNumberWithLock(request.getAccountNumber());

        withdrawalLimitChecker.checkLimit(account.getId(), request.getAmount());

        account.withdraw(request.getAmount());

        Transaction transaction = Transaction.createWithdrawal(
                account.getId(),
                request.getAmount(),
                account.getBalance()
        );
        transactionRepository.save(transaction);

        log.info("출금 완료: 계좌={}, 금액={}, 잔액={}", 
                request.getAccountNumber(), request.getAmount(), account.getBalance());

        return TransactionResponse.from(transaction, account.getAccountNumber());
    }

    /**
     * 이체
     * TODO: 데드락 가능성 제거하기
     */
    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        log.info("이체 시작: 출금계좌={}, 입금계좌={}, 금액={}",
                request.getFromAccountNumber(), request.getToAccountNumber(), request.getAmount());

        if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT, "동일한 계좌로 이체할 수 없습니다");
        }

        Account fromAccount = accountReader.findByAccountNumberWithLock(request.getFromAccountNumber());

        BigDecimal fee = feeCalculator.calculate(request.getAmount());
        BigDecimal totalAmount = request.getAmount().add(fee);

        Account toAccount = accountReader.findByAccountNumberWithLock(request.getToAccountNumber());

        transferLimitChecker.checkLimit(fromAccount.getId(), request.getAmount());

        if (!fromAccount.hasEnoughBalance(totalAmount)) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE,
                    String.format("현재 잔액: %s원, 이체 금액: %s원, 수수료: %s원",
                            fromAccount.getBalance(), request.getAmount(), fee));
        }

        fromAccount.withdraw(totalAmount);

        toAccount.deposit(request.getAmount());

        Transaction fromTransaction = Transaction.createTransferOut(
                fromAccount.getId(),
                request.getAmount(),
                fee,
                fromAccount.getBalance(),
                request.getToAccountNumber()
        );
        transactionRepository.save(fromTransaction);

        Transaction toTransaction = Transaction.createTransferIn(
                toAccount.getId(),
                request.getAmount(),
                toAccount.getBalance(),
                request.getFromAccountNumber()
        );
        transactionRepository.save(toTransaction);

        String transferId = generateTransferId(fromTransaction.getId());

        log.info("이체 완료: 출금계좌={} (잔액={}), 입금계좌={} (잔액={}), 금액={}, 수수료={}", 
                request.getFromAccountNumber(), fromAccount.getBalance(),
                request.getToAccountNumber(), toAccount.getBalance(),
                request.getAmount(), fee);

        return TransferResponse.of(
                transferId,
                TransactionResponse.from(fromTransaction, request.getFromAccountNumber()),
                TransactionResponse.from(toTransaction, request.getToAccountNumber())
        );
    }

    /**
     * 거래 내역 조회
     */
    public Page<TransactionResponse> getTransactions(String accountNumber, Pageable pageable) {
        log.info("거래 내역 조회: 계좌={}", accountNumber);

        Account account = accountReader.findByAccountNumber(accountNumber);

        Page<Transaction> transactions = transactionRepository
                .findByAccountIdOrderByCreatedAtDesc(account.getId(), pageable);

        return transactions.map(tx -> TransactionResponse.from(tx, accountNumber));
    }

    /**
     * 이체 ID 생성
     */
    private String generateTransferId(Long transactionId) {
        String date = timeProvider.now().toLocalDate().toString().replace("-", "");
        return String.format("TRF-%s-%06d", date, transactionId);
    }
}
