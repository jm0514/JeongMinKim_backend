package com.jeongminkim_backend.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeongminkim_backend.dto.request.CreateAccountRequest;
import com.jeongminkim_backend.dto.request.DepositRequest;
import com.jeongminkim_backend.repository.AccountRepository;
import com.jeongminkim_backend.repository.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTestSupport {

    // 테스트용 계좌 정보 상수
    protected static final String TEST_ACCOUNT_1 = "1234567890";
    protected static final String TEST_ACCOUNT_2 = "0987654321";
    protected static final String TEST_OWNER_1 = "홍길동";
    protected static final String TEST_OWNER_2 = "김철수";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @AfterEach
    void cleanup() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
    }

    /**
     * 테스트용 계좌 생성 헬퍼 메서드
     */
    protected void createTestAccount(String accountNumber, String ownerName) throws Exception {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .accountNumber(accountNumber)
                .ownerName(ownerName)
                .build();

        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    /**
     * 테스트용 입금 헬퍼 메서드
     */
    protected void depositToAccount(String accountNumber, BigDecimal amount) throws Exception {
        DepositRequest request = DepositRequest.builder()
                .accountNumber(accountNumber)
                .amount(amount)
                .build();

        mockMvc.perform(post("/api/v1/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
