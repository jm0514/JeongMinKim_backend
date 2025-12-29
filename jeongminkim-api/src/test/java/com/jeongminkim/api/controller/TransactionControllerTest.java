package com.jeongminkim.api.controller;

import com.jeongminkim.application.dto.request.DepositRequest;
import com.jeongminkim.application.dto.request.TransferRequest;
import com.jeongminkim.application.dto.request.WithdrawRequest;
import com.jeongminkim.api.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("TransactionController 통합 테스트")
class TransactionControllerTest extends IntegrationTestSupport {

    @Test
    @DisplayName("입금 성공 - 200 OK")
    void deposit_success() throws Exception {
        // given - 계좌 생성
        createTestAccount(TEST_ACCOUNT_1, TEST_OWNER_1);

        DepositRequest depositRequest = DepositRequest.builder()
                .accountNumber(TEST_ACCOUNT_1)
                .amount(new BigDecimal("10000"))
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value(TEST_ACCOUNT_1))
                .andExpect(jsonPath("$.data.transactionType").value("DEPOSIT"))
                .andExpect(jsonPath("$.data.amount").value(10000))
                .andExpect(jsonPath("$.data.balanceAfter").value(10000));
    }

    @Test
    @DisplayName("입금 실패 - Validation (금액 음수) - 400 Bad Request")
    void deposit_fail_validation() throws Exception {
        // given
        createTestAccount(TEST_ACCOUNT_1, TEST_OWNER_1);

        DepositRequest depositRequest = DepositRequest.builder()
                .accountNumber(TEST_ACCOUNT_1)
                .amount(new BigDecimal("-1000"))  // 음수
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("출금 성공 - 200 OK")
    void withdraw_success() throws Exception {
        // given - 계좌 생성 및 입금
        createTestAccount(TEST_ACCOUNT_1, TEST_OWNER_1);
        depositToAccount(TEST_ACCOUNT_1, new BigDecimal("50000"));

        WithdrawRequest withdrawRequest = WithdrawRequest.builder()
                .accountNumber(TEST_ACCOUNT_1)
                .amount(new BigDecimal("20000"))
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/transactions/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.transactionType").value("WITHDRAWAL"))
                .andExpect(jsonPath("$.data.amount").value(20000))
                .andExpect(jsonPath("$.data.balanceAfter").value(30000));
    }

    @Test
    @DisplayName("출금 실패 - Validation (금액 0원) - 400 Bad Request")
    void withdraw_fail_validation() throws Exception {
        // given
        createTestAccount(TEST_ACCOUNT_1, TEST_OWNER_1);
        depositToAccount(TEST_ACCOUNT_1, new BigDecimal("50000"));

        WithdrawRequest withdrawRequest = WithdrawRequest.builder()
                .accountNumber(TEST_ACCOUNT_1)
                .amount(BigDecimal.ZERO)  // 0원
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/transactions/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("이체 성공 - 수수료 계산 확인 - 200 OK")
    void transfer_success() throws Exception {
        // given - 계좌 2개 생성
        createTestAccount(TEST_ACCOUNT_1, TEST_OWNER_1);
        createTestAccount(TEST_ACCOUNT_2, TEST_OWNER_2);

        // 계좌1에 입금
        depositToAccount(TEST_ACCOUNT_1, new BigDecimal("100000"));

        // 이체 요청
        TransferRequest transferRequest = TransferRequest.builder()
                .fromAccountNumber(TEST_ACCOUNT_1)
                .toAccountNumber(TEST_ACCOUNT_2)
                .amount(new BigDecimal("50000"))
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fromTransaction.transactionType").value("TRANSFER_OUT"))
                .andExpect(jsonPath("$.data.fromTransaction.amount").value(50000))
                .andExpect(jsonPath("$.data.fromTransaction.fee").value(500))  // 1% 수수료
                .andExpect(jsonPath("$.data.fromTransaction.balanceAfter").value(49500))  // 100,000 - 50,000 - 500
                .andExpect(jsonPath("$.data.toTransaction.transactionType").value("TRANSFER_IN"))
                .andExpect(jsonPath("$.data.toTransaction.amount").value(50000))
                .andExpect(jsonPath("$.data.toTransaction.fee").value(0))  // 받는 사람은 수수료 없음
                .andExpect(jsonPath("$.data.toTransaction.balanceAfter").value(50000));
    }

    @Test
    @DisplayName("이체 실패 - Validation (금액 음수) - 400 Bad Request")
    void transfer_fail_validation() throws Exception {
        // given
        createTestAccount(TEST_ACCOUNT_1, TEST_OWNER_1);
        createTestAccount(TEST_ACCOUNT_2, TEST_OWNER_2);
        depositToAccount(TEST_ACCOUNT_1, new BigDecimal("100000"));

        TransferRequest transferRequest = TransferRequest.builder()
                .fromAccountNumber(TEST_ACCOUNT_1)
                .toAccountNumber(TEST_ACCOUNT_2)
                .amount(new BigDecimal("-10000"))  // 음수
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("거래 내역 조회 성공 - 200 OK")
    void getTransactions_success() throws Exception {
        // given - 계좌 생성 및 거래
        createTestAccount(TEST_ACCOUNT_1, TEST_OWNER_1);
        depositToAccount(TEST_ACCOUNT_1, new BigDecimal("10000"));

        // when & then
        mockMvc.perform(get("/api/v1/transactions")
                        .param("accountNumber", TEST_ACCOUNT_1)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].transactionType").value("DEPOSIT"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("전체 플로우 - 계좌 생성 → 입금 → 출금 → 이체 → 조회")
    void fullScenario() throws Exception {
        // 1. 계좌 2개 생성
        createTestAccount(TEST_ACCOUNT_1, TEST_OWNER_1);
        createTestAccount(TEST_ACCOUNT_2, TEST_OWNER_2);

        // 2. 계좌1에 100,000원 입금
        depositToAccount(TEST_ACCOUNT_1, new BigDecimal("100000"));

        mockMvc.perform(get("/api/v1/accounts/" + TEST_ACCOUNT_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(100000));

        // 3. 계좌1에서 10,000원 출금
        WithdrawRequest withdrawRequest = WithdrawRequest.builder()
                .accountNumber(TEST_ACCOUNT_1)
                .amount(new BigDecimal("10000"))
                .build();

        mockMvc.perform(post("/api/v1/transactions/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withdrawRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balanceAfter").value(90000));

        // 4. 계좌1에서 계좌2로 50,000원 이체
        TransferRequest transferRequest = TransferRequest.builder()
                .fromAccountNumber(TEST_ACCOUNT_1)
                .toAccountNumber(TEST_ACCOUNT_2)
                .amount(new BigDecimal("50000"))
                .build();

        mockMvc.perform(post("/api/v1/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fromTransaction.balanceAfter").value(39500))  // 90,000 - 50,000 - 500
                .andExpect(jsonPath("$.data.toTransaction.balanceAfter").value(50000));

        // 5. 계좌1 잔액 확인
        mockMvc.perform(get("/api/v1/accounts/" + TEST_ACCOUNT_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(39500));

        // 6. 계좌2 잔액 확인
        mockMvc.perform(get("/api/v1/accounts/" + TEST_ACCOUNT_2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(50000));

        // 7. 계좌1 거래 내역 조회 (입금 1건 + 출금 1건 + 이체 출금 1건 = 3건)
        mockMvc.perform(get("/api/v1/transactions")
                        .param("accountNumber", TEST_ACCOUNT_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.content[0].transactionType").value("TRANSFER_OUT"))
                .andExpect(jsonPath("$.data.content[1].transactionType").value("WITHDRAWAL"))
                .andExpect(jsonPath("$.data.content[2].transactionType").value("DEPOSIT"));

        // 8. 계좌2 거래 내역 조회 (이체 입금 1건)
        mockMvc.perform(get("/api/v1/transactions")
                        .param("accountNumber", TEST_ACCOUNT_2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].transactionType").value("TRANSFER_IN"));
    }
}
