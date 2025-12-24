package com.jeongminkim_backend.service;

import com.jeongminkim_backend.domain.entity.Account;
import com.jeongminkim_backend.domain.entity.Transaction;
import com.jeongminkim_backend.domain.enums.TransactionType;
import com.jeongminkim_backend.dto.request.DepositRequest;
import com.jeongminkim_backend.dto.request.TransferRequest;
import com.jeongminkim_backend.dto.request.WithdrawRequest;
import com.jeongminkim_backend.dto.response.TransactionResponse;
import com.jeongminkim_backend.dto.response.TransferResponse;
import com.jeongminkim_backend.exception.BusinessException;
import com.jeongminkim_backend.exception.ErrorCode;
import com.jeongminkim_backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    private static final BigDecimal DAILY_WITHDRAWAL_LIMIT = new BigDecimal("1000000"); // 일일 출금 한도: 100만원
    private static final BigDecimal DAILY_TRANSFER_LIMIT = new BigDecimal("3000000");   // 일일 이체 한도: 300만원

    /**
     * 입금
     */
    @Transactional
    public TransactionResponse deposit(DepositRequest request) {
        log.info("입금 시작: 계좌={}, 금액={}", request.getAccountNumber(), request.getAmount());

        Account account = accountService.findAccountByAccountNumber(request.getAccountNumber());

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

        Account account = accountService.findAccountByAccountNumberWithLock(request.getAccountNumber());

        checkDailyWithdrawalLimit(account.getId(), request.getAmount());

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

        Account fromAccount = accountService.findAccountByAccountNumberWithLock(request.getFromAccountNumber());

        BigDecimal fee = fromAccount.calculateTransferFee(request.getAmount());
        BigDecimal totalAmount = request.getAmount().add(fee);

        Account toAccount = accountService.findAccountByAccountNumberWithLock(request.getToAccountNumber());

        checkDailyTransferLimit(fromAccount.getId(), request.getAmount());

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

        Account account = accountService.findAccountByAccountNumber(accountNumber);

        Page<Transaction> transactions = transactionRepository
                .findByAccountIdOrderByCreatedAtDesc(account.getId(), pageable);

        return transactions.map(tx -> TransactionResponse.from(tx, accountNumber));
    }

    /**
     * 일일 출금 한도 체크
     */
    private void checkDailyWithdrawalLimit(Long accountId, BigDecimal amount) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        BigDecimal todayWithdrawalAmount = transactionRepository.sumAmountByAccountIdAndTypeAndDateRange(
                accountId,
                TransactionType.WITHDRAWAL,
                startOfDay,
                endOfDay
        );

        BigDecimal totalAmount = todayWithdrawalAmount.add(amount);

        if (totalAmount.compareTo(DAILY_WITHDRAWAL_LIMIT) > 0) {
            throw new BusinessException(ErrorCode.DAILY_WITHDRAWAL_LIMIT_EXCEEDED,
                    String.format("한도: %s원, 현재 사용: %s원, 요청: %s원",
                            DAILY_WITHDRAWAL_LIMIT, todayWithdrawalAmount, amount));
        }
    }

    /**
     * 일일 이체 한도 체크
     */
    private void checkDailyTransferLimit(Long accountId, BigDecimal amount) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        BigDecimal todayTransferAmount = transactionRepository.sumAmountByAccountIdAndTypeAndDateRange(
                accountId,
                TransactionType.TRANSFER_OUT,
                startOfDay,
                endOfDay
        );

        BigDecimal totalAmount = todayTransferAmount.add(amount);

        if (totalAmount.compareTo(DAILY_TRANSFER_LIMIT) > 0) {
            throw new BusinessException(ErrorCode.DAILY_TRANSFER_LIMIT_EXCEEDED,
                    String.format("한도: %s원, 현재 사용: %s원, 요청: %s원",
                            DAILY_TRANSFER_LIMIT, todayTransferAmount, amount));
        }
    }

    /**
     * 이체 ID 생성
     */
    private String generateTransferId(Long transactionId) {
        String date = LocalDate.now().toString().replace("-", "");
        return String.format("TRF-%s-%06d", date, transactionId);
    }
}
